package org.yechan.remittance.account;

import lombok.extern.slf4j.Slf4j;

public interface AccountDeleteUseCase {

  AccountModel delete(AccountDeleteProps props);
}

@Slf4j
record AccountDeleteService(
    AccountRepository accountRepository
) implements AccountDeleteUseCase {

  @Override
  public AccountModel delete(AccountDeleteProps props) {
     log.info("account.delete.start memberId={} accountId={}", props.memberId(), props.accountId());
    AccountIdentifier identifier = new AccountId(props.accountId());
    var account = accountRepository.findById(identifier)
        .orElseThrow(() -> {
           log.warn("account.delete.not_found memberId={} accountId={}", props.memberId(),
               props.accountId());
          return new AccountNotFoundException("Account not found");
        });
    if (account.memberId() != props.memberId()) {
       log.warn("account.delete.permission_denied memberId={} accountId={}", props.memberId(),
           props.accountId());
      throw new AccountPermissionDeniedException("Account owner mismatch");
    }
    accountRepository.delete(identifier);
     log.info("account.delete.success memberId={} accountId={}", props.memberId(), props.accountId());
    return account;
  }

  private record AccountId(Long accountId) implements AccountIdentifier {

  }
}
