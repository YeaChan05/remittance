package org.yechan.remittance.transfer;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

public interface TransferCreateUseCase {

  TransferResult transfer(Long memberId, String idempotencyKey, TransferRequestProps props);
}

@Slf4j
record TransferService(
    TransferIdempotencyHandler idempotencyHandler,
    TransferProcessService transferProcessService,
    LedgerWriter ledgerWriter,
    TransferSnapshotUtil transferSnapshotUtil,
    Clock clock
) implements TransferCreateUseCase {

  @Override
  public TransferResult transfer(Long memberId, String idempotencyKey, TransferRequestProps props) {
    log.info("transfer.start memberId={} scope={}", memberId, props.scope());
    LocalDateTime now = LocalDateTime.now(clock);
    var scope = props.toIdempotencyScope();
    var key = idempotencyHandler.loadKey(memberId, idempotencyKey, scope, now);
    var requestHash = transferSnapshotUtil.toHashRequest(props);

    if (key.isInvalidRequestHash(requestHash)) {
      log.warn("transfer.idempotency.conflict memberId={} scope={}", memberId, scope);
      throw new TransferIdempotencyKeyConflictException("Idempotency key conflict");
    }

    boolean marked = idempotencyHandler.markInProgress(
        memberId,
        idempotencyKey,
        scope,
        requestHash,
        now
    );

    if (!marked) {
      log.info("transfer.idempotency.existing memberId={} scope={}", memberId, scope);
      return idempotencyHandler.resolveExisting(memberId, idempotencyKey, scope, requestHash);
    }

    TransferResult result;
    try {
      result = transferProcessService.process(memberId, idempotencyKey, props, now);
    } catch (TransferFailedException ex) {
      log.warn("transfer.process.failed memberId={} scope={} code={}", memberId, scope,
          ex.getFailureCode());
      var failed = TransferResult.failed(ex.getFailureCode());
      idempotencyHandler.markFailed(memberId, idempotencyKey, scope, failed, now);
      return failed;
    }

    try {
      ledgerWriter.record(props, result, now);
    } catch (RuntimeException ex) {
       log.error("transfer.ledger.record_failed memberId={} transferId={}", memberId,
           result.transferId(), ex);
      throw new TransferLedgerRecordFailedException("Ledger record failed", ex);
    }

     log.info("transfer.success memberId={} transferId={}", memberId, result.transferId());
    return result;
  }
}
