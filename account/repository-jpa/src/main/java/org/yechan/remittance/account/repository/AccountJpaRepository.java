package org.yechan.remittance.account.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

  Optional<AccountEntity> findByMemberIdAndBankCodeAndAccountNumber(
      long memberId,
      String bankCode,
      String accountNumber
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      select a from AccountEntity a
      where a.id = :accountId
      """)
  Optional<AccountEntity> findByIdForUpdate(@Param("accountId") Long accountId);
}
