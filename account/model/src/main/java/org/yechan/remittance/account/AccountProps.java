package org.yechan.remittance.account;

import java.math.BigDecimal;

public interface AccountProps {

  Long memberId();

  String bankCode();

  String accountNumber();

  String accountName();

  BigDecimal balance();
}
