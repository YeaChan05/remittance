package org.yechan.remittance.account;

import java.util.Optional;

public interface AccountRepository {

  AccountModel save(AccountProps props);

  Optional<AccountModel> findById(AccountIdentifier identifier);

  Optional<AccountModel> findByMemberIdAndBankCodeAndAccountNumber(
      long memberId,
      String bankCode,
      String accountNumber
  );

  void delete(AccountIdentifier identifier);
}
