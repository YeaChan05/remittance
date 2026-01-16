package org.yechan.remittance.transfer;

import java.math.BigDecimal;

public interface DailyLimitUsageModel extends DailyLimitUsageProps, DailyLimitUsageIdentifier {

  void updateUsedAmount(BigDecimal usedAmount);
}
