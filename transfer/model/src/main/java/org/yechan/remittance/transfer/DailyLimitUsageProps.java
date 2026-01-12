package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public interface DailyLimitUsageProps {

  Long accountId();

  TransferScopeValue scope();

  LocalDate usageDate();

  BigDecimal usedAmount();
}
