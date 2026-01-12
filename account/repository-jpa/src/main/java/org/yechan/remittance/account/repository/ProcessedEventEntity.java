package org.yechan.remittance.account.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.yechan.remittance.BaseEntity;

@Entity
@Table(name = "processed_events", schema = "integration")
class ProcessedEventEntity extends BaseEntity {

  @Column(nullable = false, unique = true)
  private Long eventId;

  @Column(nullable = false)
  private LocalDateTime processedAt;

  protected ProcessedEventEntity() {
  }

  private ProcessedEventEntity(Long eventId, LocalDateTime processedAt) {
    this.eventId = eventId;
    this.processedAt = processedAt;
  }

  static ProcessedEventEntity create(Long eventId, LocalDateTime processedAt) {
    return new ProcessedEventEntity(eventId, processedAt);
  }

  Long eventId() {
    return eventId;
  }

  LocalDateTime processedAt() {
    return processedAt;
  }
}
