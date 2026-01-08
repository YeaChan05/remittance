package org.yechan.remittance.account;

import java.math.BigDecimal;

public record Account(
    Long accountId,
    Long memberId,
    String bankCode,
    String accountNumber,
    String accountName,
    BigDecimal balance
) implements AccountModel {

}
