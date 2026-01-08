package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.yechan.remittance.transfer.OutboxEventModel;
import org.yechan.remittance.transfer.OutboxEventProps;

@Entity
@Table(name = "outbox_events", schema = "integration")
public class OutboxEventEntity implements OutboxEventModel {

  @Id
  @Column(nullable = false)
  private String eventId;

  @Column(nullable = false)
  private String aggregateType;

  @Column(nullable = false)
  private String aggregateId;

  @Column(nullable = false)
  private String eventType;

  @Column(nullable = false)
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OutboxEventStatusValue status;

  @Column(nullable = false)
  private Instant createdAt;

  protected OutboxEventEntity() {
  }

  private OutboxEventEntity(
      String eventId,
      String aggregateType,
      String aggregateId,
      String eventType,
      String payload,
      OutboxEventStatusValue status,
      Instant createdAt
  ) {
    this.eventId = eventId;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.status = status;
    this.createdAt = createdAt;
  }

  static OutboxEventEntity create(OutboxEventProps props, String eventId) {
    return new OutboxEventEntity(
        eventId,
        props.aggregateType(),
        props.aggregateId(),
        props.eventType(),
        props.payload(),
        props.status(),
        props.createdAt()
    );
  }

  @Override
  public String eventId() {
    return eventId;
  }

  @Override
  public String aggregateType() {
    return aggregateType;
  }

  @Override
  public String aggregateId() {
    return aggregateId;
  }

  @Override
  public String eventType() {
    return eventType;
  }

  @Override
  public String payload() {
    return payload;
  }

  @Override
  public OutboxEventStatusValue status() {
    return status;
  }

  @Override
  public Instant createdAt() {
    return createdAt;
  }
}
