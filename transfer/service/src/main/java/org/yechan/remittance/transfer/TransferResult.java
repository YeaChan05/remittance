package org.yechan.remittance.transfer;

import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

public record TransferResult(
    TransferStatusValue status,
    Long transferId,
    String errorCode
) {

  public static TransferResult inProgress() {
    return new TransferResult(TransferStatusValue.IN_PROGRESS, null, null);
  }

  public static TransferResult succeeded(Long transferId) {
    return new TransferResult(TransferStatusValue.SUCCEEDED, transferId, null);
  }

  public static TransferResult failed(TransferFailureCode errorCode) {
    return new TransferResult(TransferStatusValue.FAILED, null, errorCode.name());
  }
}
