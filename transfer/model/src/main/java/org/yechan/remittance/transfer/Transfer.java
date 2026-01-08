package org.yechan.remittance.transfer;

import java.time.Instant;

public record Transfer(
    Long transferId,
    Long fromAccountId,
    Long toAccountId,
    Long amount,
    TransferScopeValue scope,
    TransferStatusValue status,
    Instant requestedAt,
    Instant completedAt
) implements TransferModel {

}
