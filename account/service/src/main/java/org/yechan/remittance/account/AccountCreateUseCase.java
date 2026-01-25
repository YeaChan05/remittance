package org.yechan.remittance.account;

import lombok.extern.slf4j.Slf4j;

public interface AccountCreateUseCase {

  AccountModel create(AccountProps props);
}

@Slf4j
record AccountService(
    AccountRepository accountRepository
) implements AccountCreateUseCase {

  @Override
  public AccountModel create(AccountProps props) {
    log.info("account.create.start memberId={} bankCode={}", props.memberId(), props.bankCode());
    accountRepository.findByMemberIdAndBankCodeAndAccountNumber(
            props.memberId(),
            props.bankCode(),
            props.accountNumber())
        .ifPresent(account -> {
          log.warn("account.create.duplicate memberId={} bankCode={}", props.memberId(),
              props.bankCode());
          throw new AccountDuplicateException("Account already exists");
        });
    log.info("account.create.persist memberId={} bankCode={}", props.memberId(), props.bankCode());
    return accountRepository.save(props);
  }
}
