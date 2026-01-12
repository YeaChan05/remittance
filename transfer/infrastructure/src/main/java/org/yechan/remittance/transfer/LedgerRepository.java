package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.yechan.remittance.transfer.LedgerProps.LedgerSideValue;

public interface LedgerRepository {

  LedgerModel save(LedgerProps props);

  boolean existsByTransferIdAndAccountIdAndSide(Long transferId, Long accountId,
      LedgerSideValue side);

  BigDecimal sumAmountByAccountIdAndSideBetween(
      Long accountId,
      LedgerSideValue side,
      LocalDateTime from,
      LocalDateTime to
  );
}
