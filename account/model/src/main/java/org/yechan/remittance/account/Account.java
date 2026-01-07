package org.yechan.remittance.account;

public record Account(
    Long accountId,
    long memberId,
    String bankCode,
    String accountNumber,
    String accountName
) implements AccountModel {

}
