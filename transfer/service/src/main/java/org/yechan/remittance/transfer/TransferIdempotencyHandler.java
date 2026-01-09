package org.yechan.remittance.transfer;

import static org.yechan.remittance.transfer.TransferSnapshotUtil.*;

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
    var key = repository.findByKey(memberId, scope, idempotencyKey)
        .orElseThrow(() -> new TransferIdempotencyKeyNotFoundException("Idempotency key not found"));
    if (key.expiresAt() != null && key.expiresAt().isBefore(now)) {
      throw new TransferIdempotencyKeyExpiredException("Idempotency key expired");
    }
    return key;
  }

  public void validateRequestHash(IdempotencyKeyModel key, String requestHash) {
    if (key.requestHash() != null && !key.requestHash().equals(requestHash)) {
      throw new TransferIdempotencyKeyConflictException("Idempotency key conflict");
    }
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
    IdempotencyKeyModel existing = repository.findByKey(
            memberId,
            scope,
            idempotencyKey)
        .orElseThrow(() -> new TransferIdempotencyKeyNotFoundException("Idempotency key not found"));
    validateRequestHash(existing, requestHash);
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
