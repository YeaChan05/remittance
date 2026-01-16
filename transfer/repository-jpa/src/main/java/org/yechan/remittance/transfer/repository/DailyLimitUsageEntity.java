package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.transfer.DailyLimitUsageModel;
import org.yechan.remittance.transfer.DailyLimitUsageProps;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

@Entity
@Table(
    name = "daily_limit_usage",
    schema = "core",
    uniqueConstraints =
    @UniqueConstraint(
        name = "uk_daily_limit_usage_account_scope_date",
        columnNames = {"account_id", "scope", "usage_date"}
    )
)
public class DailyLimitUsageEntity extends BaseEntity implements DailyLimitUsageModel {

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferScopeValue scope;

  @Column(name = "usage_date", nullable = false)
  private LocalDate usageDate;

  @Column(name = "used_amount", nullable = false)
  private BigDecimal usedAmount;

  protected DailyLimitUsageEntity() {
  }

  private DailyLimitUsageEntity(
      Long accountId,
      TransferScopeValue scope,
      LocalDate usageDate,
      BigDecimal usedAmount
  ) {
    this.accountId = accountId;
    this.scope = scope;
    this.usageDate = usageDate;
    this.usedAmount = usedAmount;
  }

  static DailyLimitUsageEntity create(DailyLimitUsageProps props) {
    return new DailyLimitUsageEntity(
        props.accountId(),
        props.scope(),
        props.usageDate(),
        props.usedAmount()
    );
  }

  @Override
  public Long dailyLimitUsageId() {
    return super.getId();
  }

  @Override
  public Long accountId() {
    return accountId;
  }

  @Override
  public TransferScopeValue scope() {
    return scope;
  }

  @Override
  public LocalDate usageDate() {
    return usageDate;
  }

  @Override
  public BigDecimal usedAmount() {
    return usedAmount;
  }

  @Override
  public void updateUsedAmount(BigDecimal usedAmount) {
    this.usedAmount = usedAmount;
  }
}
