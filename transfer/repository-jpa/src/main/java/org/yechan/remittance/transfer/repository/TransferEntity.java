package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.transfer.TransferModel;
import org.yechan.remittance.transfer.TransferProps;

@Entity
@Table(name = "transfer", schema = "core")
public class TransferEntity extends BaseEntity implements TransferModel {

  @Column(nullable = false)
  private Long fromAccountId;

  @Column(nullable = false)
  private Long toAccountId;

  @Column(nullable = false)
  private Long amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferScopeValue scope;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferStatusValue status;

  @Column(nullable = false)
  private Instant requestedAt;

  @Column
  private Instant completedAt;

  protected TransferEntity() {
  }

  private TransferEntity(
      Long fromAccountId,
      Long toAccountId,
      Long amount,
      TransferScopeValue scope,
      TransferStatusValue status,
      Instant completedAt
  ) {
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.amount = amount;
    this.scope = scope;
    this.status = status;
    this.requestedAt = Instant.now();
    this.completedAt = completedAt;
  }

  static TransferEntity create(TransferProps props) {
    return new TransferEntity(
        props.fromAccountId(),
        props.toAccountId(),
        props.amount(),
        props.scope(),
        props.status(),
        props.completedAt()
    );
  }

  @Override
  public Long transferId() {
    return super.getId();
  }

  @Override
  public Long fromAccountId() {
    return fromAccountId;
  }

  @Override
  public Long toAccountId() {
    return toAccountId;
  }

  @Override
  public Long amount() {
    return amount;
  }

  @Override
  public TransferScopeValue scope() {
    return scope;
  }

  @Override
  public TransferStatusValue status() {
    return status;
  }

  @Override
  public Instant requestedAt() {
    return requestedAt;
  }

  @Override
  public Instant completedAt() {
    return completedAt;
  }
}
