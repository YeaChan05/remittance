package org.yechan.remittance.ledger;

import java.time.Instant;
import java.util.Optional;
import org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyScopeValue;

public interface IdempotencyKeyRepository {

  IdempotencyKeyModel save(IdempotencyKeyProps props);

  Optional<IdempotencyKeyModel> findByMemberIdAndIdempotencyKey(
      Long memberId,
      String idempotencyKey
  );

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
      Instant startedAt
  );

  IdempotencyKeyModel markSucceeded(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      Instant completedAt
  );

  IdempotencyKeyModel markFailed(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      Instant completedAt
  );

  int markTimeoutBefore(Instant cutoff, String responseSnapshot);
}
