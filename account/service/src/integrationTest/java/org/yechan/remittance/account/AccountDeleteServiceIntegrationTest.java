package org.yechan.remittance.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AccountDeleteServiceIntegrationTest {

  @Test
  void deletesAccountWhenOwnerMatches() {
    var deletedId = new AtomicReference<Long>();
    AccountRepository repository = new TestAccountRepository(
        Optional.of(new TestAccount(1L, 10L)),
        deletedId
    );
    AccountDeleteUseCase useCase = new AccountDeleteService(repository);

    AccountModel deleted = useCase.delete(new TestDeleteProps(1L, 10L));

    assertThat(deleted.accountId()).isEqualTo(10L);
    assertThat(deletedId.get()).isEqualTo(10L);
  }

  @Test
  void throwsWhenOwnerDoesNotMatch() {
    AccountRepository repository = new TestAccountRepository(
        Optional.of(new TestAccount(2L, 10L)),
        new AtomicReference<>()
    );
    AccountDeleteUseCase useCase = new AccountDeleteService(repository);

    assertThatThrownBy(() -> useCase.delete(new TestDeleteProps(1L, 10L)))
        .isInstanceOf(AccountPermissionDeniedException.class)
        .hasMessage("Account owner mismatch");
  }

  @Test
  void throwsWhenAccountDoesNotExist() {
    AccountRepository repository = new TestAccountRepository(Optional.empty(),
        new AtomicReference<>());
    AccountDeleteUseCase useCase = new AccountDeleteService(repository);

    assertThatThrownBy(() -> useCase.delete(new TestDeleteProps(1L, 10L)))
        .isInstanceOf(AccountNotFoundException.class)
        .hasMessage("Account not found");
  }

  private static class TestAccountRepository implements AccountRepository {

    private final Optional<AccountModel> account;
    private final AtomicReference<Long> deletedId;

    private TestAccountRepository(Optional<AccountModel> account, AtomicReference<Long> deletedId) {
      this.account = account;
      this.deletedId = deletedId;
    }

    @Override
    public AccountModel save(AccountProps props) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<AccountModel> findById(AccountIdentifier identifier) {
      return account;
    }

    @Override
    public Optional<AccountModel> findByIdForUpdate(AccountIdentifier identifier) {
      return Optional.empty();
    }

    @Override
    public Optional<AccountModel> findByMemberIdAndBankCodeAndAccountNumber(
        Long memberId,
        String bankCode,
        String accountNumber
    ) {
      return Optional.empty();
    }

    @Override
    public AccountModel updateBalance(AccountIdentifier identifier, Long balance) {
      return null;
    }

    @Override
    public void delete(AccountIdentifier identifier) {
      deletedId.set(identifier.accountId());
    }
  }

  private record TestDeleteProps(long memberId, long accountId) implements AccountDeleteProps {

  }

  private record TestAccount(Long memberId, Long accountId) implements AccountModel {

    @Override
    public String bankCode() {
      return "001";
    }

    @Override
    public String accountNumber() {
      return "123";
    }

    @Override
    public String accountName() {
      return "name";
    }

    @Override
    public BigDecimal balance() {
      return BigDecimal.ZERO;
    }
  }
}
