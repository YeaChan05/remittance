package org.yechan.remittance.ledger.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.ledger.IdempotencyKeyModel;
import org.yechan.remittance.ledger.IdempotencyKeyProps;

@Entity
@Table(name = "idempotency_key")
public class IdempotencyKeyEntity extends BaseEntity implements IdempotencyKeyModel {

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false, unique = true)
  private String idempotencyKey;

  @Column(nullable = false)
  private Instant expiresAt;

  protected IdempotencyKeyEntity() {
  }

  private IdempotencyKeyEntity(Long memberId, String idempotencyKey, Instant expiresAt) {
    this.memberId = memberId;
    this.idempotencyKey = idempotencyKey;
    this.expiresAt = expiresAt;
  }

  static IdempotencyKeyEntity create(IdempotencyKeyProps props) {
    return new IdempotencyKeyEntity(props.memberId(), props.idempotencyKey(), props.expiresAt());
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
  public Instant expiresAt() {
    return expiresAt;
  }

  @Override
  public Long idempotencyKeyId() {
    return super.getId();
  }
}
