package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;

public interface TransferProps {

  Long fromAccountId();

  Long toAccountId();

  BigDecimal amount();

  TransferScopeValue scope();

  TransferStatusValue status();

  LocalDateTime requestedAt();

  LocalDateTime completedAt();

  enum TransferScopeValue {
    DEPOSIT,
    WITHDRAW,
    TRANSFER;

    IdempotencyScopeValue toIdempotencyScope(
    ) {
      return switch (this) {
        case TRANSFER -> IdempotencyScopeValue.TRANSFER;
        case WITHDRAW -> IdempotencyScopeValue.WITHDRAW;
        case DEPOSIT -> IdempotencyScopeValue.DEPOSIT;
      };
    }
  }

  enum TransferStatusValue {
    IN_PROGRESS,
    SUCCEEDED,
    FAILED
  }
}
