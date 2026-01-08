package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Ledger(
    Long ledgerId,
    Long transferId,
    Long accountId,
    BigDecimal amount,
    LedgerSideValue side,
    LocalDateTime createdAt
) implements LedgerModel {

}
