package org.yechan.remittance.account.repository;

import java.util.Optional;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.account.AccountModel;
import org.yechan.remittance.account.AccountProps;
import org.yechan.remittance.account.AccountRepository;

public class AccountRepositoryImpl implements AccountRepository {

  private final AccountJpaRepository repository;

  public AccountRepositoryImpl(AccountJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public AccountModel save(AccountProps props) {
    return repository.save(AccountEntity.create(props));
  }

  @Override
  public Optional<AccountModel> findById(AccountIdentifier identifier) {
    return repository.findById(identifier.accountId())
        .map(account -> account);
  }

  @Override
  public Optional<AccountModel> findByIdForUpdate(AccountIdentifier identifier) {
    return repository.findByIdForUpdate(identifier.accountId())
        .map(account -> account);
  }

  @Override
  public Optional<AccountModel> findByMemberIdAndBankCodeAndAccountNumber(
      Long memberId,
      String bankCode,
      String accountNumber
  ) {
    return repository.findByMemberIdAndBankCodeAndAccountNumber(memberId, bankCode, accountNumber)
        .map(account -> account);
  }

  @Override
  public AccountModel updateBalance(AccountIdentifier identifier, Long balance) {
    repository.updateBalance(identifier.accountId(), balance);
    return repository.findById(identifier.accountId()).orElseThrow();
  }

  @Override
  public void delete(AccountIdentifier identifier) {
    repository.deleteById(identifier.accountId());
  }
}
