package org.yechan.remittance.transfer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.yechan.remittance.transfer.TransferProps;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TransferRepositoryAutoConfiguration.class)
@ContextConfiguration(classes = TestApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TransferJpaRepositoryTest {

  private static final List<TransferStatusValue> COMPLETED_STATUSES = List.of(
      TransferStatusValue.SUCCEEDED,
      TransferStatusValue.FAILED
  );

  private final TransferJpaRepository repository;
  private final EntityManager entityManager;

  @Autowired
  TransferJpaRepositoryTest(
      TransferJpaRepository repository,
      EntityManager entityManager
  ) {
    this.repository = repository;
    this.entityManager = entityManager;
  }

  @Test
  void findCompletedByAccountIdFiltersAndOrders() {
    LocalDateTime now = LocalDateTime.parse("2026-02-01T00:00:00");
    saveTransfer(1L, 2L, TransferStatusValue.SUCCEEDED, now.minusSeconds(120));
    saveTransfer(3L, 1L, TransferStatusValue.FAILED, now.minusSeconds(30));
    saveTransfer(1L, 4L, TransferStatusValue.IN_PROGRESS, null);
    saveTransfer(5L, 6L, TransferStatusValue.SUCCEEDED, now.minusSeconds(10));
    flushClear();

    List<TransferEntity> results = repository.findCompletedByAccountId(
        1L,
        COMPLETED_STATUSES,
        now.minusSeconds(300),
        now,
        Pageable.unpaged()
    );

    assertThat(results).hasSize(2);
    assertThat(results).isSortedAccordingTo(Comparator.comparing(TransferEntity::completedAt).reversed());
    assertThat(results.get(0).status()).isEqualTo(TransferStatusValue.FAILED);
    assertThat(results.get(1).status()).isEqualTo(TransferStatusValue.SUCCEEDED);
    assertThat(results.get(0).completedAt()).isAfter(results.get(1).completedAt());
  }

  @Test
  void findCompletedByAccountIdRespectsLimitAndRange() {
    LocalDateTime now = LocalDateTime.parse("2026-02-02T00:00:00");
    saveTransfer(1L, 2L, TransferStatusValue.SUCCEEDED, now.minusSeconds(100));
    saveTransfer(1L, 3L, TransferStatusValue.SUCCEEDED, now.minusSeconds(50));
    saveTransfer(1L, 4L, TransferStatusValue.SUCCEEDED, now.minusSeconds(10));
    flushClear();

    List<TransferEntity> results = repository.findCompletedByAccountId(
        1L,
        COMPLETED_STATUSES,
        now.minusSeconds(60),
        now,
        PageRequest.of(0, 1)
    );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).completedAt()).isEqualTo(now.minusSeconds(10));
  }

  private void saveTransfer(
      Long fromAccountId,
      Long toAccountId,
      TransferStatusValue status,
      LocalDateTime completedAt
  ) {
    repository.save(
        TransferEntity.create(new TestTransferProps(
            fromAccountId,
            toAccountId,
            status,
            completedAt == null ? LocalDateTime.parse("2026-02-01T00:00:00") : completedAt,
            completedAt
        ))
    );
  }

  private void flushClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private record TestTransferProps(
      Long fromAccountId,
      Long toAccountId,
      TransferStatusValue status,
      LocalDateTime requestedAt,
      LocalDateTime completedAt
  ) implements TransferProps {

    @Override
    public BigDecimal amount() {
      return BigDecimal.valueOf(1000L);
    }

    @Override
    public TransferScopeValue scope() {
      return TransferScopeValue.DEPOSIT;
    }
  }
}
