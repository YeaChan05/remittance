package org.yechan.remittance.account.repository;

import java.time.LocalDateTime;
import org.yechan.remittance.account.ProcessedEventRepository;

class ProcessedEventRepositoryImpl implements ProcessedEventRepository {

  private final ProcessedEventJpaRepository repository;

  ProcessedEventRepositoryImpl(ProcessedEventJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean existsByEventId(Long eventId) {
    return repository.existsByEventId(eventId);
  }

  @Override
  public void markProcessed(Long eventId, LocalDateTime processedAt) {
    repository.save(ProcessedEventEntity.create(eventId, processedAt));
  }
}
