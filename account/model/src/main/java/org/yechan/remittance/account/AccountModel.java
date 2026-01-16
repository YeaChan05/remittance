package org.yechan.remittance.account;

import java.math.BigDecimal;

public interface AccountModel extends AccountProps, AccountIdentifier {

  default void updateBalance(BigDecimal balance) {
    throw new UnsupportedOperationException("Balance update not supported");
  }
}
