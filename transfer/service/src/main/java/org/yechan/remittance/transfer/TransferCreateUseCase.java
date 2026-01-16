package org.yechan.remittance.transfer;

import static org.yechan.remittance.transfer.TransferSnapshotUtil.toHashRequest;

import java.time.Clock;
import java.time.LocalDateTime;

public interface TransferCreateUseCase {

  TransferResult transfer(Long memberId, String idempotencyKey, TransferRequestProps props);
}

record TransferService(
    TransferIdempotencyHandler idempotencyHandler,
    TransferProcessService transferProcessService,
    LedgerWriter ledgerWriter,
    Clock clock
) implements TransferCreateUseCase {

  @Override
  public TransferResult transfer(Long memberId, String idempotencyKey, TransferRequestProps props) {
    LocalDateTime now = LocalDateTime.now(clock);
    var scope = props.toIdempotencyScope();
    var key = idempotencyHandler.loadKey(memberId, idempotencyKey, scope, now);
    var requestHash = toHashRequest(props);

    if (key.isInvalidRequestHash(requestHash)) {
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
      return idempotencyHandler.resolveExisting(memberId, idempotencyKey, scope, requestHash);
    }

    TransferResult result;
    try {
      result = transferProcessService.process(memberId, idempotencyKey, props, now);
    } catch (TransferFailedException ex) {
      var failed = TransferResult.failed(ex.getFailureCode());
      idempotencyHandler.markFailed(memberId, idempotencyKey, scope, failed, now);
      return failed;
    }

    try {
      ledgerWriter.record(props, result, now);
    } catch (RuntimeException ex) {
      throw new TransferLedgerRecordFailedException("Ledger record failed", ex);
    }

    return result;
  }
}
