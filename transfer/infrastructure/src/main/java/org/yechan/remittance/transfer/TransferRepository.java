package org.yechan.remittance.transfer;

import java.util.List;
import java.util.Optional;
import org.yechan.remittance.account.AccountIdentifier;

public interface TransferRepository {

  TransferModel save(TransferProps props);

  Optional<TransferModel> findById(TransferIdentifier identifier);

  List<? extends TransferModel> findCompletedByAccountId(
      AccountIdentifier identifier,
      TransferQueryCondition condition
  );
}
