package org.yechan.remittance.account;

public interface AccountCreateUseCase {

  AccountModel create(AccountProps props);
}


class AccountService implements AccountCreateUseCase {

  private final AccountRepository accountRepository;

  AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public AccountModel create(AccountProps props) {
    accountRepository.findByMemberIdAndBankCodeAndAccountNumber(
            props.memberId(),
            props.bankCode(),
            props.accountNumber())
        .ifPresent(account -> {
          throw new AccountDuplicateException("Account already exists");
        });
    return accountRepository.save(props);
  }
}
