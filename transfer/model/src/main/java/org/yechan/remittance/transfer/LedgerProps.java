package org.yechan.remittance.transfer;

import java.time.LocalDateTime;

public interface LedgerProps {

  Long transferId();

  Long accountId();

  Long amount();

  LedgerSideValue side();

  LocalDateTime createdAt();

  enum LedgerSideValue {
    DEBIT,
    CREDIT
  }
}
