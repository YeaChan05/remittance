package org.yechan.remittance.transfer;

import static org.yechan.remittance.transfer.TransferSnapshotUtil.fromSnapshot;
import static org.yechan.remittance.transfer.TransferSnapshotUtil.toSnapshot;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;

public class TransferIdempotencyHandler {

  private final IdempotencyKeyRepository repository;

  public TransferIdempotencyHandler(IdempotencyKeyRepository repository) {
    this.repository = repository;
  }

  public IdempotencyKeyModel loadKey(
      Long memberId,
      String idempotencyKey,
      IdempotencyScopeValue scope,
      LocalDateTime now
  ) {
    var key = getIdempotencyKey(memberId, idempotencyKey, scope);
    if (key.isExpired(now)) {
      throw new TransferIdempotencyKeyExpiredException("Idempotency key expired");
    }
    return key;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean markInProgress(
      Long memberId,
      String idempotencyKey,
      IdempotencyScopeValue scope,
      String requestHash,
      LocalDateTime now
  ) {
    return repository.tryMarkInProgress(
        memberId,
        scope,
        idempotencyKey,
        requestHash,
        now
    );
  }

  public TransferResult resolveExisting(
      Long memberId,
      String idempotencyKey,
      IdempotencyScopeValue scope,
      String requestHash
  ) {
    var existing = getIdempotencyKey(memberId, idempotencyKey, scope);

    if (existing.isInvalidRequestHash(requestHash)) {
      throw new TransferIdempotencyKeyConflictException("Idempotency key conflict");
    }

    if (existing.status() == IdempotencyKeyStatusValue.IN_PROGRESS) {
      return TransferResult.inProgress();
    }

    if (existing.responseSnapshot() == null) {
      return TransferResult.inProgress();
    }

    return fromSnapshot(existing.responseSnapshot());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailed(
      Long memberId,
      String idempotencyKey,
      IdempotencyScopeValue scope,
      TransferResult failed,
      LocalDateTime now
  ) {
    repository.markFailed(
        memberId,
        scope,
        idempotencyKey,
        toSnapshot(failed),
        now
    );
  }
}
