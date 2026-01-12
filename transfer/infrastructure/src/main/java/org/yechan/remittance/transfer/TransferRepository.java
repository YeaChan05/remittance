package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public interface TransferRepository {

  TransferModel save(TransferRequestProps props);

  Optional<TransferModel> findById(TransferIdentifier identifier);

  List<? extends TransferModel> findCompletedByAccountId(
      AccountIdentifier identifier,
      TransferQueryCondition condition
  );

  BigDecimal sumAmountByFromAccountIdAndScopeBetween(
      AccountIdentifier identifier,
      TransferScopeValue scope,
      LocalDateTime from,
      LocalDateTime to
  );
}
