package org.yechan.remittance.transfer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;

interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

  Optional<IdempotencyKeyEntity> findByMemberIdAndScopeAndIdempotencyKey(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey
  );

  List<IdempotencyKeyEntity> findByStatusAndStartedAtBefore(
      IdempotencyKeyStatusValue status,
      LocalDateTime startedAt
  );
}
