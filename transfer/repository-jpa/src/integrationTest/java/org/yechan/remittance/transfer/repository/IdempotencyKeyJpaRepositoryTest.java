package org.yechan.remittance.transfer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.yechan.remittance.transfer.IdempotencyKeyModel;
import org.yechan.remittance.transfer.IdempotencyKeyProps;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;
import org.yechan.remittance.transfer.IdempotencyKeyRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TransferRepositoryAutoConfiguration.class)
@ContextConfiguration(classes = TestApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class IdempotencyKeyJpaRepositoryTest {

  private final IdempotencyKeyRepository repository;
  private final EntityManager entityManager;

  @Autowired
  IdempotencyKeyJpaRepositoryTest(
      IdempotencyKeyRepository repository,
      EntityManager entityManager
  ) {
    this.repository = repository;
    this.entityManager = entityManager;
  }

  @Test
  void markInProgressUpdatesOnlyWhenBeforeStart() {
    LocalDateTime now = LocalDateTime.parse("2026-01-01T00:00:00");
    IdempotencyKeyModel saved = saveIdempotencyKey(now, 10L, "idem-key");
    flushClear();

    boolean updated = repository.tryMarkInProgress(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "hash",
        now
    );
    flushClear();

    assertThat(updated).isTrue();
    var found = repository.findByKey(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey()
    );
    assertThat(found).isPresent();
    assertThat(found.get().status()).isEqualTo(IdempotencyKeyStatusValue.IN_PROGRESS);
    assertThat(found.get().requestHash()).isEqualTo("hash");
    assertThat(found.get().startedAt()).isEqualTo(now);

    boolean secondUpdate = repository.tryMarkInProgress(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "hash-2",
        now.plusSeconds(30)
    );
    assertThat(secondUpdate).isFalse();
  }

  @Test
  void markSucceededUpdatesSnapshotAndCompletedAt() {
    LocalDateTime now = LocalDateTime.parse("2026-01-02T00:00:00");
    IdempotencyKeyModel saved = saveIdempotencyKey(now, 20L, "idem-succeed");
    flushClear();

    repository.tryMarkInProgress(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "hash",
        now
    );
    repository.markSucceeded(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "{\"status\":\"SUCCEEDED\"}",
        now.plusSeconds(30)
    );
    flushClear();

    var found = repository.findByKey(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey()
    );
    assertThat(found).isPresent();
    assertThat(found.get().status()).isEqualTo(IdempotencyKeyStatusValue.SUCCEEDED);
    assertThat(found.get().responseSnapshot()).contains("SUCCEEDED");
    assertThat(found.get().completedAt()).isEqualTo(now.plusSeconds(30));
  }

  @Test
  void markFailedUpdatesSnapshotAndCompletedAt() {
    LocalDateTime now = LocalDateTime.parse("2026-01-03T00:00:00");
    IdempotencyKeyModel saved = saveIdempotencyKey(now, 30L, "idem-failed");
    flushClear();

    repository.tryMarkInProgress(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "hash",
        now
    );
    repository.markFailed(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "{\"status\":\"FAILED\"}",
        now.plusSeconds(10)
    );
    flushClear();

    var found = repository.findByKey(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey()
    );
    assertThat(found).isPresent();
    assertThat(found.get().status()).isEqualTo(IdempotencyKeyStatusValue.FAILED);
    assertThat(found.get().responseSnapshot()).contains("FAILED");
    assertThat(found.get().completedAt()).isEqualTo(now.plusSeconds(10));
  }

  private void flushClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void markTimeoutBeforeMovesOldInProgressToTimeout() {
    LocalDateTime now = LocalDateTime.parse("2026-01-04T00:00:00");
    IdempotencyKeyModel saved = saveIdempotencyKey(now, 40L, "idem-timeout");
    flushClear();

    repository.tryMarkInProgress(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey(),
        "hash",
        now.minus(Duration.ofMinutes(10))
    );
    flushClear();

    int updated = repository.markTimeoutBefore(
        now.minus(Duration.ofMinutes(5)),
        "{\"status\":\"FAILED\",\"error_code\":\"TIMEOUT\"}"
    );
    flushClear();

    assertThat(updated).isEqualTo(1);
    var found = repository.findByKey(
        saved.memberId(),
        IdempotencyScopeValue.TRANSFER,
        saved.idempotencyKey()
    );
    assertThat(found).isPresent();
    assertThat(found.get().status()).isEqualTo(IdempotencyKeyStatusValue.TIMEOUT);
    assertThat(found.get().responseSnapshot()).contains("TIMEOUT");
  }

  private IdempotencyKeyModel saveIdempotencyKey(
      LocalDateTime now,
      Long memberId,
      String idempotencyKey
  ) {
    return repository.save(new TestIdempotencyKeyProps(memberId, idempotencyKey, now));
  }

  private record TestIdempotencyKeyProps(
      Long memberId, String idempotencyKey, LocalDateTime now
  ) implements IdempotencyKeyProps {

    @Override
    public Long memberId() {
      return memberId;
    }

    @Override
    public String idempotencyKey() {
      return idempotencyKey;
    }

    @Override
    public LocalDateTime expiresAt() {
      return now.plus(Duration.ofMinutes(20));
    }

    @Override
    public IdempotencyScopeValue scope() {
      return IdempotencyScopeValue.TRANSFER;
    }

    @Override
    public IdempotencyKeyStatusValue status() {
      return null;
    }

    @Override
    public String requestHash() {
      return null;
    }

    @Override
    public String responseSnapshot() {
      return null;
    }

    @Override
    public LocalDateTime startedAt() {
      return null;
    }

    @Override
    public LocalDateTime completedAt() {
      return null;
    }
  }
}
