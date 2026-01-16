package org.yechan.remittance.transfer;

import java.time.LocalDateTime;
import java.util.Optional;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;

public interface IdempotencyKeyRepository {

  IdempotencyKeyModel save(IdempotencyKeyProps props);

  Optional<IdempotencyKeyModel> findByKey(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey
  );

  boolean tryMarkInProgress(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String requestHash,
      LocalDateTime startedAt
  );

  IdempotencyKeyModel markSucceeded(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      LocalDateTime completedAt
  );

  IdempotencyKeyModel markFailed(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      LocalDateTime completedAt
  );

  int markTimeoutBefore(LocalDateTime cutoff, String responseSnapshot);
}
