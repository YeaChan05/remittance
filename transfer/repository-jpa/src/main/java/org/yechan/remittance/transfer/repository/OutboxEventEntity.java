package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.transfer.OutboxEventModel;
import org.yechan.remittance.transfer.OutboxEventProps;

@Entity
@Table(name = "outbox_events", catalog = "integration")
public class OutboxEventEntity extends BaseEntity implements OutboxEventModel {


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


  protected OutboxEventEntity() {
  }

  private OutboxEventEntity(
      String aggregateType,
      String aggregateId,
      String eventType,
      String payload,
      OutboxEventStatusValue status
  ) {
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.eventType = eventType;
    this.payload = payload;
    this.status = status;
  }

  static OutboxEventEntity create(OutboxEventProps props) {
    return new OutboxEventEntity(
        props.aggregateType(),
        props.aggregateId(),
        props.eventType(),
        props.payload(),
        props.status()
    );
  }

  @Override
  public Long eventId() {
    return super.getId();
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

}
