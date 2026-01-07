package org.yechan.remittance.ledger;

import java.time.Instant;

public record IdempotencyKey(
    Long idempotencyKeyId,
    Long memberId,
    String idempotencyKey,
    Instant expiresAt
) implements IdempotencyKeyModel {

}
