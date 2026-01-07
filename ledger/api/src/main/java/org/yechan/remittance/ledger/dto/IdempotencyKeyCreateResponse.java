package org.yechan.remittance.ledger.dto;

import java.time.Instant;

public record IdempotencyKeyCreateResponse(
    String idempotencyKey,
    Instant expiresAt
) {

}
