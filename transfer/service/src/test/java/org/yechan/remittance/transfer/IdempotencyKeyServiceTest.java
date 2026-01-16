package org.yechan.remittance.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;

class IdempotencyKeyServiceTest {

  @Test
  void createsIdempotencyKeyWithExpiration() {
    var saved = new AtomicReference<IdempotencyKeyProps>();
    IdempotencyKeyRepository repository = new TestIdempotencyKeyRepository(saved);
    LocalDateTime now = LocalDateTime.parse("2026-01-01T00:00:00");
    Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    var properties = new IdempotencyKeyProperties(Duration.ofHours(1));
    var service = new IdempotencyKeyService(repository, clock, properties.expiresIn());

    IdempotencyKeyModel created = service.create(new IdempotencyKeyCreateProps() {
      @Override
      public long memberId() {
        return 10L;
      }

      @Override
      public IdempotencyScopeValue scope() {
        return IdempotencyScopeValue.TRANSFER;
      }
    });

    assertThat(created.memberId()).isEqualTo(10L);
    assertThat(created.idempotencyKey()).isNotBlank();
    assertThat(created.expiresAt()).isEqualTo(now.plus(Duration.ofHours(1)));
    assertThat(created.scope()).isEqualTo(IdempotencyKeyProps.IdempotencyScopeValue.TRANSFER);
    assertThat(created.status()).isEqualTo(IdempotencyKeyStatusValue.BEFORE_START);
    assertThat(created.requestHash()).isNull();
    assertThat(created.responseSnapshot()).isNull();
    assertThat(created.startedAt()).isNull();
    assertThat(created.completedAt()).isNull();
    assertThat(saved.get()).isNotNull();
    assertThat(saved.get().idempotencyKey()).isEqualTo(created.idempotencyKey());
  }

  private record TestIdempotencyKeyRepository(AtomicReference<IdempotencyKeyProps> saved) implements
      IdempotencyKeyRepository {

    @Override
    public IdempotencyKeyModel save(IdempotencyKeyProps props) {
      saved.set(props);
      return new IdempotencyKey(
          saved.get().memberId(),
          props.memberId(),
          props.idempotencyKey(),
          props.expiresAt(),
          props.scope(),
          props.status(),
          props.requestHash(),
          props.responseSnapshot(),
          props.startedAt(),
          props.completedAt()
      );
    }

    @Override
    public Optional<IdempotencyKeyModel> findByKey(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey
    ) {
      return Optional.empty();
    }

    @Override
    public boolean tryMarkInProgress(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey,
        String requestHash,
        LocalDateTime startedAt
    ) {
      return false;
    }

    @Override
    public IdempotencyKeyModel markSucceeded(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey,
        String responseSnapshot,
        LocalDateTime completedAt
    ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public IdempotencyKeyModel markFailed(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey,
        String responseSnapshot,
        LocalDateTime completedAt
    ) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int markTimeoutBefore(LocalDateTime cutoff, String responseSnapshot) {
      return 0;
    }
  }
}
