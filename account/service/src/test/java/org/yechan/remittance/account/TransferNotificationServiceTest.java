package org.yechan.remittance.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class TransferNotificationServiceTest {

  @Test
  void notifyPushesAndMarksProcessed() {
    var accountRepository = new TestAccountRepository(Optional.of(sampleAccount()));
    var processedRepository = new TestProcessedEventRepository(false);
    var pushPort = new TestNotificationPushPort();
    var service = new TransferNotificationService(
        accountRepository,
        processedRepository,
        pushPort
    );

    service.notify(sampleProps());

    assertEquals(1, pushPort.pushCount.get());
    assertTrue(processedRepository.markedEventId.get() != null);
  }

  @Test
  void notifySkipsWhenAlreadyProcessed() {
    var accountRepository = new TestAccountRepository(Optional.of(sampleAccount()));
    var processedRepository = new TestProcessedEventRepository(true);
    var pushPort = new TestNotificationPushPort();
    var service = new TransferNotificationService(
        accountRepository,
        processedRepository,
        pushPort
    );

    service.notify(sampleProps());

    assertEquals(0, pushPort.pushCount.get());
  }

  @Test
  void notifyMarksProcessedWhenPushFails() {
    var accountRepository = new TestAccountRepository(Optional.of(sampleAccount()));
    var processedRepository = new TestProcessedEventRepository(false);
    var pushPort = new TestNotificationPushPort();
    pushPort.fail = true;
    var service = new TransferNotificationService(
        accountRepository,
        processedRepository,
        pushPort
    );

    service.notify(sampleProps());

    assertTrue(processedRepository.markedEventId.get() != null);
  }

  private AccountModel sampleAccount() {
    return new Account(
        10L,
        99L,
        "090",
        "123-456",
        "생활비",
        BigDecimal.ZERO
    );
  }

  private TransferNotificationProps sampleProps() {
    return new TransferNotificationProps() {
      @Override
      public Long eventId() {
        return 1L;
      }

      @Override
      public Long transferId() {
        return 11L;
      }

      @Override
      public Long toAccountId() {
        return 10L;
      }

      @Override
      public Long fromAccountId() {
        return 1L;
      }

      @Override
      public BigDecimal amount() {
        return BigDecimal.valueOf(10000);
      }

      @Override
      public LocalDateTime occurredAt() {
        return LocalDateTime.of(2025, 1, 1, 0, 0);
      }
    };
  }

  private static class TestAccountRepository implements AccountRepository {

    private final Optional<AccountModel> account;

    private TestAccountRepository(Optional<AccountModel> account) {
      this.account = account;
    }

    @Override
    public AccountModel save(AccountProps props) {
      throw new UnsupportedOperationException("Not used");
    }

    @Override
    public Optional<AccountModel> findById(AccountIdentifier identifier) {
      return account;
    }

    @Override
    public Optional<AccountModel> findByIdForUpdate(AccountIdentifier identifier) {
      throw new UnsupportedOperationException("Not used");
    }

    @Override
    public Optional<AccountModel> findByMemberIdAndBankCodeAndAccountNumber(
        Long memberId,
        String bankCode,
        String accountNumber
    ) {
      throw new UnsupportedOperationException("Not used");
    }

    @Override
    public void delete(AccountIdentifier identifier) {
      throw new UnsupportedOperationException("Not used");
    }
  }

  private static class TestProcessedEventRepository implements ProcessedEventRepository {

    private final boolean processed;
    private final AtomicReference<Long> markedEventId = new AtomicReference<>();

    private TestProcessedEventRepository(boolean processed) {
      this.processed = processed;
    }

    @Override
    public boolean existsByEventId(Long eventId) {
      return processed;
    }

    @Override
    public void markProcessed(Long eventId, LocalDateTime processedAt) {
      markedEventId.set(eventId);
    }
  }

  private static class TestNotificationPushPort implements NotificationPushPort {

    private final AtomicInteger pushCount = new AtomicInteger();
    private boolean fail;

    @Override
    public boolean push(Long memberId, TransferNotificationMessage message) {
      if (fail) {
        throw new IllegalStateException("fail");
      }
      pushCount.incrementAndGet();
      return true;
    }
  }
}
