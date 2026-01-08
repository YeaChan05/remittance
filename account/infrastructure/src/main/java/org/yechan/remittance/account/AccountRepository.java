package org.yechan.remittance.account;

import java.util.Optional;

public interface AccountRepository {

  AccountModel save(AccountProps props);

  Optional<AccountModel> findById(AccountIdentifier identifier);

  Optional<AccountModel> findByIdForUpdate(AccountIdentifier identifier);

  Optional<AccountModel> findByMemberIdAndBankCodeAndAccountNumber(
      Long memberId,
      String bankCode,
      String accountNumber
  );

  AccountModel updateBalance(AccountIdentifier identifier, Long balance);

  void delete(AccountIdentifier identifier);
}
