package org.yechan.remittance.account;

public interface AccountDeleteUseCase {

  AccountModel delete(AccountDeleteProps props);
}


class AccountDeleteService implements AccountDeleteUseCase {

  private final AccountRepository accountRepository;

  AccountDeleteService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public AccountModel delete(AccountDeleteProps props) {
    AccountIdentifier identifier = new AccountId(props.accountId());
    var account = accountRepository.findById(identifier)
        .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    if (account.memberId() != props.memberId()) {
      throw new AccountPermissionDeniedException("Account owner mismatch");
    }
    accountRepository.delete(identifier);
    return account;
  }

  private record AccountId(Long accountId) implements AccountIdentifier {

  }
}
