package org.yechan.remittance.transfer.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

interface TransferJpaRepository extends JpaRepository<TransferEntity, Long> {

  @Query("""
      select t from TransferEntity t
      where (t.fromAccountId = :accountId or t.toAccountId = :accountId)
        and t.status in :statuses
        and (:from is null or t.completedAt >= :from)
        and (:to is null or t.completedAt <= :to)
      order by t.completedAt desc
      """)
  List<TransferEntity> findCompletedByAccountId(
      @Param("accountId") Long accountId,
      @Param("statuses") List<TransferStatusValue> statuses,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable
  );
}
