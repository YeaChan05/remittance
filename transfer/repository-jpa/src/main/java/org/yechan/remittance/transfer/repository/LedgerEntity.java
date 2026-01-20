package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.transfer.LedgerModel;
import org.yechan.remittance.transfer.LedgerProps;

@Entity
@Table(
    name = "ledger",
    catalog = "core",
    uniqueConstraints =
    @UniqueConstraint(
        name = "uk_ledger_transfer_account_side",
        columnNames = {"transfer_id", "account_id", "side"}
    )
)
public class LedgerEntity extends BaseEntity implements LedgerModel {

  @Column(name = "transfer_id", nullable = false)
  private Long transferId;

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LedgerSideValue side;

  protected LedgerEntity() {
  }

  private LedgerEntity(
      Long transferId,
      Long accountId,
      BigDecimal amount,
      LedgerSideValue side
  ) {
    this.transferId = transferId;
    this.accountId = accountId;
    this.amount = amount;
    this.side = side;
  }

  static LedgerEntity create(LedgerProps props) {
    return new LedgerEntity(
        props.transferId(),
        props.accountId(),
        props.amount(),
        props.side()
    );
  }

  @Override
  public Long ledgerId() {
    return super.getId();
  }

  @Override
  public Long transferId() {
    return transferId;
  }

  @Override
  public Long accountId() {
    return accountId;
  }

  @Override
  public BigDecimal amount() {
    return amount;
  }

  @Override
  public LedgerSideValue side() {
    return side;
  }

  @Override
  public LocalDateTime createdAt() {
    return super.getCreatedAt();
  }
}
