package org.yechan.remittance.transfer;

import java.math.BigDecimal;

public interface DailyLimitUsageModel extends DailyLimitUsageProps {

  void updateUsedAmount(BigDecimal usedAmount);
}
