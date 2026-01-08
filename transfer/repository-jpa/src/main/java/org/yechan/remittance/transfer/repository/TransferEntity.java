package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferScopeValue scope;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferStatusValue status;

  @Column(nullable = false)
  private LocalDateTime requestedAt;

  @Column
  private LocalDateTime completedAt;

  protected TransferEntity() {
  }

  private TransferEntity(
      Long fromAccountId,
      Long toAccountId,
      BigDecimal amount,
      TransferScopeValue scope,
      TransferStatusValue status,
      LocalDateTime completedAt
  ) {
    this.fromAccountId = fromAccountId;
    this.toAccountId = toAccountId;
    this.amount = amount;
    this.scope = scope;
    this.status = status;
    this.requestedAt = LocalDateTime.now();
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
  public BigDecimal amount() {
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
  public LocalDateTime requestedAt() {
    return requestedAt;
  }

  @Override
  public LocalDateTime completedAt() {
    return completedAt;
  }
}
