package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    TRANSFER,
    REFUND
  }

  enum TransferStatusValue {
    IN_PROGRESS,
    SUCCEEDED,
    FAILED
  }
}
