package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transfer(
    Long transferId,
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount,
    TransferScopeValue scope,
    TransferStatusValue status,
    LocalDateTime requestedAt,
    LocalDateTime completedAt
) implements TransferModel {

}
