package org.yechan.remittance.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.yechan.remittance.transfer.TransferModel;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

public record TransferQueryResponse(
    List<TransferItem> transfers
) {

  public static TransferQueryResponse from(List<? extends TransferModel> transfers) {
    return new TransferQueryResponse(transfers.stream()
        .map(TransferItem::from)
        .toList());
  }

  public record TransferItem(
      Long transferId,
      Long fromAccountId,
      Long toAccountId,
      BigDecimal amount,
      TransferScopeValue scope,
      TransferStatusValue status,
      LocalDateTime requestedAt,
      LocalDateTime completedAt
  ) {

    public static TransferItem from(TransferModel transfer) {
      return new TransferItem(
          transfer.transferId(),
          transfer.fromAccountId(),
          transfer.toAccountId(),
          transfer.amount(),
          transfer.scope(),
          transfer.status(),
          transfer.requestedAt(),
          transfer.completedAt()
      );
    }
  }
}
