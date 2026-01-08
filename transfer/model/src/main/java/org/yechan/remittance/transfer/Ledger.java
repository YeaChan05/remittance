package org.yechan.remittance.transfer;

import java.time.Instant;

public record Ledger(
    Long ledgerId,
    Long transferId,
    Long accountId,
    Long amount,
    LedgerSideValue side,
    Instant createdAt
) implements LedgerModel {

}
