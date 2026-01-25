package org.yechan.remittance.transfer;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;

@Slf4j
public class TransferIdempotencyHandler {

  private final IdempotencyKeyRepository repository;
  private final TransferSnapshotUtil transferSnapshotUtil;

  public TransferIdempotencyHandler(
      IdempotencyKeyRepository repository,
      TransferSnapshotUtil transferSnapshotUtil
  ) {
    this.repository = repository;
    this.transferSnapshotUtil = transferSnapshotUtil;
  }

  public IdempotencyKeyModel loadKey(
      Long memberId,
      String idempotencyKey,
      IdempotencyScopeValue scope,
      LocalDateTime now
  ) {
    log.info("transfer.idempotency.load memberId={} scope={}", memberId, scope);
    var key = getIdempotencyKey(memberId, idempotencyKey, scope);
    if (key.isExpired(now)) {
      log.warn("transfer.idempotency.expired memberId={} scope={}", memberId, scope);
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
    log.debug("transfer.idempotency.mark_in_progress memberId={} scope={}", memberId, scope);
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
    log.info("transfer.idempotency.resolve memberId={} scope={}", memberId, scope);
    var existing = getIdempotencyKey(memberId, idempotencyKey, scope);

    if (existing.isInvalidRequestHash(requestHash)) {
      log.warn("transfer.idempotency.conflict memberId={} scope={}", memberId, scope);
      throw new TransferIdempotencyKeyConflictException("Idempotency key conflict");
    }

    if (existing.status() == IdempotencyKeyStatusValue.IN_PROGRESS) {
      log.info("transfer.idempotency.in_progress memberId={} scope={}", memberId, scope);
      return TransferResult.inProgress();
    }

    if (existing.responseSnapshot() == null) {
      log.info("transfer.idempotency.no_snapshot memberId={} scope={}", memberId, scope);
      return TransferResult.inProgress();
    }

    return transferSnapshotUtil.fromSnapshot(existing.responseSnapshot());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailed(
      Long memberId,
      String idempotencyKey,
      IdempotencyScopeValue scope,
      TransferResult failed,
      LocalDateTime now
  ) {
    log.warn("transfer.idempotency.mark_failed memberId={} scope={}", memberId, scope);
    repository.markFailed(
        memberId,
        scope,
        idempotencyKey,
        transferSnapshotUtil.toSnapshot(failed),
        now
    );
  }

  private IdempotencyKeyModel getIdempotencyKey(Long memberId, String idempotencyKey,
      IdempotencyScopeValue scope) {
    return repository.findByKey(memberId, scope, idempotencyKey)
        .orElseThrow(
            () -> new TransferIdempotencyKeyNotFoundException("Idempotency key not found"));
  }
}
