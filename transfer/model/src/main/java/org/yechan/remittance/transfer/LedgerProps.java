package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LedgerProps {

  Long transferId();

  Long accountId();

  BigDecimal amount();

  LedgerSideValue side();

  LocalDateTime createdAt();

  enum LedgerSideValue {
    DEBIT,
    CREDIT
  }
}
