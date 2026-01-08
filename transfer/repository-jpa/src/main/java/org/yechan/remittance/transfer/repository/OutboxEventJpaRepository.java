package org.yechan.remittance.transfer.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.yechan.remittance.transfer.OutboxEventProps.OutboxEventStatusValue;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

  @Query("""
      select o from OutboxEventEntity o
      where o.status = :status
        and (:before is null or o.createdAt <= :before)
      order by o.createdAt asc
      """)
  List<OutboxEventEntity> findNewForPublish(
      @Param("status") OutboxEventStatusValue status,
      @Param("before") Instant before,
      Pageable pageable
  );

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update OutboxEventEntity o
      set o.status = :status
      where o.id = :eventId
      """)
  int markSent(
      @Param("eventId") Long eventId,
      @Param("status") OutboxEventStatusValue status
  );
}
