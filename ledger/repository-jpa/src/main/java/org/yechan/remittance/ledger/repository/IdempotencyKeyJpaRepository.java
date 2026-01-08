package org.yechan.remittance.ledger.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyScopeValue;

interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

  Optional<IdempotencyKeyEntity> findByMemberIdAndIdempotencyKey(
      Long memberId,
      String idempotencyKey
  );

  Optional<IdempotencyKeyEntity> findByMemberIdAndScopeAndIdempotencyKey(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey
  );

  @Modifying
  @Query("""
      update IdempotencyKeyEntity
         set status = org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyKeyStatusValue.IN_PROGRESS,
             requestHash = :requestHash,
             startedAt = :startedAt
       where memberId = :memberId
         and scope = :scope
         and idempotencyKey = :idempotencyKey
         and status = org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyKeyStatusValue.BEFORE_START
      """)
  Integer markInProgress(
      @Param("memberId") Long memberId,
      @Param("scope") IdempotencyScopeValue scope,
      @Param("idempotencyKey") String idempotencyKey,
      @Param("requestHash") String requestHash,
      @Param("startedAt") Instant startedAt
  );

  @Modifying
  @Query("""
      update IdempotencyKeyEntity
         set status = org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyKeyStatusValue.SUCCEEDED,
             responseSnapshot = :responseSnapshot,
             completedAt = :completedAt
       where memberId = :memberId
         and scope = :scope
         and idempotencyKey = :idempotencyKey
      """)
  Integer markSucceeded(
      @Param("memberId") Long memberId,
      @Param("scope") IdempotencyScopeValue scope,
      @Param("idempotencyKey") String idempotencyKey,
      @Param("responseSnapshot") String responseSnapshot,
      @Param("completedAt") Instant completedAt
  );

  @Modifying
  @Query("""
      update IdempotencyKeyEntity
         set status = org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyKeyStatusValue.FAILED,
             responseSnapshot = :responseSnapshot,
             completedAt = :completedAt
       where memberId = :memberId
         and scope = :scope
         and idempotencyKey = :idempotencyKey
      """)
  Integer markFailed(
      @Param("memberId") Long memberId,
      @Param("scope") IdempotencyScopeValue scope,
      @Param("idempotencyKey") String idempotencyKey,
      @Param("responseSnapshot") String responseSnapshot,
      @Param("completedAt") Instant completedAt
  );

  @Modifying
  @Query("""
      update IdempotencyKeyEntity
         set status = org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyKeyStatusValue.TIMEOUT,
             responseSnapshot = :responseSnapshot,
             completedAt = :completedAt
       where status = org.yechan.remittance.ledger.IdempotencyKeyProps.IdempotencyKeyStatusValue.IN_PROGRESS
         and startedAt < :cutoff
      """)
  Integer markTimeoutBefore(
      @Param("cutoff") Instant cutoff,
      @Param("responseSnapshot") String responseSnapshot,
      @Param("completedAt") Instant completedAt
  );
}
