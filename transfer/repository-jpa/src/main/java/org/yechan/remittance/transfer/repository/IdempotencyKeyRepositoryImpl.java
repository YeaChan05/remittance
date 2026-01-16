package org.yechan.remittance.transfer.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.yechan.remittance.transfer.IdempotencyKeyModel;
import org.yechan.remittance.transfer.IdempotencyKeyProps;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;
import org.yechan.remittance.transfer.IdempotencyKeyRepository;

record IdempotencyKeyRepositoryImpl(
    IdempotencyKeyJpaRepository repository
) implements IdempotencyKeyRepository {

  @Override
  public IdempotencyKeyModel save(IdempotencyKeyProps props) {
    return repository.save(IdempotencyKeyEntity.create(props));
  }

  @Override
  public Optional<IdempotencyKeyModel> findByKey(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey
  ) {
    return repository.findByMemberIdAndScopeAndIdempotencyKey(memberId, scope, idempotencyKey)
        .map(item -> item);
  }

  @Override
  public boolean tryMarkInProgress(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String requestHash,
      LocalDateTime startedAt
  ) {
    var found = repository.findByMemberIdAndScopeAndIdempotencyKey(memberId, scope, idempotencyKey);
    if (found.isEmpty()) {
      return false;
    }

    var entity = found.get();
    boolean updated = entity.tryMarkInProgress(requestHash, startedAt);

    if (updated) {
      repository.save(entity);
    }
    return updated;
  }

  @Override
  public IdempotencyKeyModel markSucceeded(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    var entity = repository
        .findByMemberIdAndScopeAndIdempotencyKey(memberId, scope, idempotencyKey)
        .orElseThrow();

    entity.markSucceeded(responseSnapshot, completedAt);

    return repository.save(entity);
  }

  @Override
  public IdempotencyKeyModel markFailed(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    var entity = repository
        .findByMemberIdAndScopeAndIdempotencyKey(memberId, scope, idempotencyKey)
        .orElseThrow();

    entity.markFailed(responseSnapshot, completedAt);
    return repository.save(entity);
  }

  @Override
  public int markTimeoutBefore(LocalDateTime cutoff, String responseSnapshot) {
    var completedAt = LocalDateTime.now();
    var candidates = repository.findByStatusAndStartedAtBefore(
        IdempotencyKeyStatusValue.IN_PROGRESS,
        cutoff
    );

    int updated = (int) candidates.stream()
        .filter(entity -> entity.markTimeoutIfBefore(cutoff, responseSnapshot, completedAt))
        .count();
    repository.saveAll(candidates);
    return updated;
  }
}
