package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yechan.remittance.transfer.LedgerProps.LedgerSideValue;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

@Slf4j
public class LedgerWriter {

  private final LedgerRepository ledgerRepository;

  public LedgerWriter(LedgerRepository ledgerRepository) {
    this.ledgerRepository = ledgerRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(TransferRequestProps props, TransferResult result, LocalDateTime now) {
    if (result.transferId() == null) {
      log.info("ledger.record.skip transferId=null");
      return;
    }
    if (props.scope() == TransferScopeValue.DEPOSIT) {
      log.info("ledger.record.deposit transferId={}", result.transferId());
      saveLedgerIfAbsent(
          result.transferId(),
          props.toAccountId(),
          props.amount(),
          LedgerSideValue.CREDIT,
          now
      );
      return;
    }

    BigDecimal debitAmount = props.debit();
    log.info("ledger.record.debit transferId={} fromAccountId={}", result.transferId(),
        props.fromAccountId());
    saveLedgerIfAbsent(
        result.transferId(),
        props.fromAccountId(),
        debitAmount,
        LedgerSideValue.DEBIT,
        now
    );

    if (props.scope() == TransferScopeValue.TRANSFER) {
      log.info("ledger.record.credit transferId={} toAccountId={}", result.transferId(),
          props.toAccountId());
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
      log.debug("ledger.record.exists transferId={} accountId={} side={}", transferId, accountId,
          side);
      return;
    }
    log.info("ledger.record.save transferId={} accountId={} side={}", transferId, accountId, side);
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
