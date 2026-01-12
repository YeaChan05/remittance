package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yechan.remittance.transfer.LedgerProps.LedgerSideValue;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public class LedgerWriter {

  private final LedgerRepository ledgerRepository;

  public LedgerWriter(LedgerRepository ledgerRepository) {
    this.ledgerRepository = ledgerRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(TransferRequestProps props, TransferResult result, LocalDateTime now) {
    if (result.transferId() == null) {
      return;
    }
    if (props.scope() == TransferScopeValue.DEPOSIT) {
      saveLedgerIfAbsent(
          result.transferId(),
          props.toAccountId(),
          props.amount(),
          LedgerSideValue.CREDIT,
          now
      );
      return;
    }

    BigDecimal debitAmount = props.amount().add(props.fee());
    saveLedgerIfAbsent(
        result.transferId(),
        props.fromAccountId(),
        debitAmount,
        LedgerSideValue.DEBIT,
        now
    );

    if (props.scope() == TransferScopeValue.TRANSFER) {
      saveLedgerIfAbsent(
          result.transferId(),
          props.toAccountId(),
          props.amount(),
          LedgerSideValue.CREDIT,
          now
      );
    }
  }

  private void saveLedgerIfAbsent(
      Long transferId,
      Long accountId,
      BigDecimal amount,
      LedgerSideValue side,
      LocalDateTime now
  ) {
    if (ledgerRepository.existsByTransferIdAndAccountIdAndSide(transferId, accountId, side)) {
      return;
    }
    ledgerRepository.save(new LedgerCreateCommand(transferId, accountId, amount, side, now));
  }

  private record LedgerCreateCommand(
      Long transferId,
      Long accountId,
      BigDecimal amount,
      LedgerSideValue side,
      LocalDateTime createdAt
  ) implements LedgerProps {

  }
}
