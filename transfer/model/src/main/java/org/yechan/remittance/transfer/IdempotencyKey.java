package org.yechan.remittance.transfer;

import java.time.LocalDateTime;

public record IdempotencyKey(
    Long idempotencyKeyId,
    Long memberId,
    String idempotencyKey,
    LocalDateTime expiresAt,
    IdempotencyScopeValue scope,
    IdempotencyKeyStatusValue status,
    String requestHash,
    String responseSnapshot,
    LocalDateTime startedAt,
    LocalDateTime completedAt
) implements IdempotencyKeyModel {

}
