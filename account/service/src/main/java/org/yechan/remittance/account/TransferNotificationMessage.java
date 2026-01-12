package org.yechan.remittance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferNotificationMessage(
    String type,
    Long transferId,
    BigDecimal amount,
    Long fromAccountId,
    LocalDateTime occurredAt
) {

}
