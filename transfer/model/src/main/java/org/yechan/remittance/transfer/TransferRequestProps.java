package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public interface TransferRequestProps {

  Long fromAccountId();

  Long toAccountId();

  BigDecimal amount();

  TransferScopeValue scope();

  BigDecimal fee();
}
