package org.yechan.remittance.account.repository;

import java.time.LocalDateTime;
import org.yechan.remittance.account.ProcessedEventRepository;

record ProcessedEventRepositoryImpl(
    ProcessedEventJpaRepository repository
) implements ProcessedEventRepository {

  @Override
  public boolean existsByEventId(Long eventId) {
    return repository.existsByEventId(eventId);
  }

  @Override
  public void markProcessed(Long eventId, LocalDateTime processedAt) {
    repository.save(ProcessedEventEntity.create(eventId, processedAt));
  }
}
