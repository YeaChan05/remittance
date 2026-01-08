package org.yechan.remittance.transfer.dto;

import java.time.LocalDateTime;

public record IdempotencyKeyCreateResponse(
    String idempotencyKey,
    LocalDateTime expiresAt
) {

}
