package org.yechan.remittance.transfer.repository;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.Optional;
import org.yechan.remittance.transfer.IdempotencyKeyModel;
import org.yechan.remittance.transfer.IdempotencyKeyProps;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;
import org.yechan.remittance.transfer.IdempotencyKeyRepository;

public class IdempotencyKeyRepositoryImpl implements IdempotencyKeyRepository {

  private final IdempotencyKeyJpaRepository repository;

  public IdempotencyKeyRepositoryImpl(IdempotencyKeyJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public IdempotencyKeyModel save(IdempotencyKeyProps props) {
    return repository.save(IdempotencyKeyEntity.create(props));
  }

  @Override
  public Optional<IdempotencyKeyModel> findByMemberIdAndIdempotencyKey(
      Long memberId,
      String idempotencyKey
  ) {
    return repository.findByMemberIdAndIdempotencyKey(memberId, idempotencyKey).map(item -> item);
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
    return repository.markInProgress(memberId, scope, idempotencyKey, requestHash, startedAt) > 0;
  }

  @Override
  public IdempotencyKeyModel markSucceeded(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    repository.markSucceeded(memberId, scope, idempotencyKey, responseSnapshot, completedAt);
    return repository.findByMemberIdAndScopeAndIdempotencyKey(memberId, scope, idempotencyKey)
        .orElseThrow();
  }

  @Override
  public IdempotencyKeyModel markFailed(
      Long memberId,
      IdempotencyScopeValue scope,
      String idempotencyKey,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    repository.markFailed(memberId, scope, idempotencyKey, responseSnapshot, completedAt);
    return repository.findByMemberIdAndScopeAndIdempotencyKey(memberId, scope, idempotencyKey)
        .orElseThrow();
  }

  @Override
  public int markTimeoutBefore(LocalDateTime cutoff, String responseSnapshot) {
    return repository.markTimeoutBefore(cutoff, responseSnapshot, LocalDateTime.now());
  }
}
