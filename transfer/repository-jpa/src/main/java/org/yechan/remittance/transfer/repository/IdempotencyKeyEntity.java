package org.yechan.remittance.transfer.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.transfer.IdempotencyKeyModel;
import org.yechan.remittance.transfer.IdempotencyKeyProps;

@Entity
@Table(
    name = "idempotency_key",
    catalog = "integration",
    uniqueConstraints =
    @UniqueConstraint(
        name = "uk_idempotency_key_client_scope",
        columnNames = {"client_id", "scope", "idempotency_key"}
    )
)
public class IdempotencyKeyEntity extends BaseEntity implements IdempotencyKeyModel {

  @Column(name = "client_id", nullable = false)
  private Long memberId;

  @Column(name = "idempotency_key", nullable = false)
  private String idempotencyKey;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope", nullable = false)
  private IdempotencyScopeValue scope;

  @Enumerated(EnumType.STRING)
  @Column
  private IdempotencyKeyStatusValue status;

  @Column
  private String requestHash;

  @Column
  private String responseSnapshot;

  @Column
  private LocalDateTime startedAt;

  @Column
  private LocalDateTime completedAt;

  protected IdempotencyKeyEntity() {
  }

  private IdempotencyKeyEntity(
      Long memberId,
      String idempotencyKey,
      LocalDateTime expiresAt,
      IdempotencyScopeValue scope
  ) {
    this.memberId = memberId;
    this.idempotencyKey = idempotencyKey;
    this.expiresAt = expiresAt;
    this.scope = scope;
    this.status = IdempotencyKeyStatusValue.BEFORE_START;
    this.requestHash = null;
    this.responseSnapshot = null;
    this.startedAt = null;
    this.completedAt = null;
  }

  static IdempotencyKeyEntity create(IdempotencyKeyProps props) {
    return new IdempotencyKeyEntity(
        props.memberId(),
        props.idempotencyKey(),
        props.expiresAt(),
        props.scope()
    );
  }

  public boolean isExpired(LocalDateTime now) {
    return expiresAt != null && expiresAt.isBefore(now);
  }

  public boolean tryMarkInProgress(String requestHash, LocalDateTime startedAt) {
    if (status != IdempotencyKeyStatusValue.BEFORE_START) {
      return false;
    }
    this.status = IdempotencyKeyStatusValue.IN_PROGRESS;
    this.requestHash = requestHash;
    this.startedAt = startedAt;
    return true;
  }

  public void markSucceeded(String responseSnapshot, LocalDateTime completedAt) {
    this.status = IdempotencyKeyStatusValue.SUCCEEDED;
    this.responseSnapshot = responseSnapshot;
    this.completedAt = completedAt;
  }

  public void markFailed(String responseSnapshot, LocalDateTime completedAt) {
    this.status = IdempotencyKeyStatusValue.FAILED;
    this.responseSnapshot = responseSnapshot;
    this.completedAt = completedAt;
  }

  public boolean markTimeoutIfBefore(
      LocalDateTime cutoff,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    if (status != IdempotencyKeyStatusValue.IN_PROGRESS) {
      return false;
    }
    if (startedAt == null || !startedAt.isBefore(cutoff)) {
      return false;
    }
    this.status = IdempotencyKeyStatusValue.TIMEOUT;
    this.responseSnapshot = responseSnapshot;
    this.completedAt = completedAt;
    return true;
  }

  @Override
  public Long memberId() {
    return memberId;
  }

  @Override
  public String idempotencyKey() {
    return idempotencyKey;
  }

  @Override
  public LocalDateTime expiresAt() {
    return expiresAt;
  }

  @Override
  public IdempotencyScopeValue scope() {
    return scope;
  }

  @Override
  public IdempotencyKeyStatusValue status() {
    return status;
  }

  @Override
  public String requestHash() {
    return requestHash;
  }

  @Override
  public String responseSnapshot() {
    return responseSnapshot;
  }

  @Override
  public LocalDateTime startedAt() {
    return startedAt;
  }

  @Override
  public LocalDateTime completedAt() {
    return completedAt;
  }

  @Override
  public Long idempotencyKeyId() {
    return super.getId();
  }
}
