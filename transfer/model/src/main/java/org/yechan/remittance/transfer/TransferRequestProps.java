package org.yechan.remittance.transfer;

import java.math.BigDecimal;

public interface TransferRequestProps {

  Long fromAccountId();

  Long toAccountId();

  BigDecimal amount();
}
