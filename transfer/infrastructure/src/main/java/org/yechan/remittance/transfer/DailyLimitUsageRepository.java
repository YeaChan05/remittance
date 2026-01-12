package org.yechan.remittance.transfer;

import java.time.LocalDate;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public interface DailyLimitUsageRepository {

  DailyLimitUsageModel findOrCreateForUpdate(
      AccountIdentifier identifier,
      TransferScopeValue scope,
      LocalDate usageDate
  );
}
