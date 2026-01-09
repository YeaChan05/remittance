package org.yechan.remittance.transfer;

import static org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue.TRANSFER;
import static org.yechan.remittance.transfer.TransferSnapshotUtil.hashRequest;

import java.time.Clock;
import java.time.LocalDateTime;

public interface TransferCreateUseCase {

  TransferResult transfer(Long memberId, String idempotencyKey, TransferRequestProps props);
}

class TransferService implements TransferCreateUseCase {

  private final TransferIdempotencyHandler idempotencyHandler;
  private final TransferProcessService transferProcessService;
  private final LedgerWriter ledgerWriter;
  private final Clock clock;

  TransferService(
      TransferIdempotencyHandler idempotencyHandler,
      TransferProcessService transferProcessService,
      LedgerWriter ledgerWriter,
      Clock clock
  ) {
    this.idempotencyHandler = idempotencyHandler;
    this.transferProcessService = transferProcessService;
    this.ledgerWriter = ledgerWriter;
    this.clock = clock;
  }

  @Override
  public TransferResult transfer(Long memberId, String idempotencyKey, TransferRequestProps props) {
    LocalDateTime now = LocalDateTime.now(clock);
    var key = idempotencyHandler.loadKey(memberId, idempotencyKey, TRANSFER, now);
    var requestHash = hashRequest(props);

    idempotencyHandler.validateRequestHash(key, requestHash);

    boolean marked = idempotencyHandler.markInProgress(
        memberId,
        idempotencyKey,
        TRANSFER,
        requestHash,
        now
    );

    if (!marked) {
      return idempotencyHandler.resolveExisting(memberId, idempotencyKey, TRANSFER, requestHash);
    }

    TransferResult result;
    try {
      result = transferProcessService.process(memberId, idempotencyKey, props, now);
    } catch (TransferFailedException ex) {
      TransferResult failed = TransferResult.failed(ex.getFailureCode());
      idempotencyHandler.markFailed(memberId, idempotencyKey, TRANSFER, failed, now);
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
