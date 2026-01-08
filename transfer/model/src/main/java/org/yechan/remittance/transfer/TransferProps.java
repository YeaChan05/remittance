package org.yechan.remittance.transfer;

import java.time.Instant;

public interface TransferProps {

  Long fromAccountId();

  Long toAccountId();

  Long amount();

  TransferScopeValue scope();

  TransferStatusValue status();

  Instant requestedAt();

  Instant completedAt();

  enum TransferScopeValue {
    DEPOSIT,
    WITHDRAW,
    REFUND
  }

  enum TransferStatusValue {
    IN_PROGRESS,
    SUCCEEDED,
    FAILED
  }
}
