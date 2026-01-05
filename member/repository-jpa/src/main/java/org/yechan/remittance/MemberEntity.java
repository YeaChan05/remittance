package org.yechan.remittance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
class MemberEntity extends BaseEntity implements MemberModel {

  @Column(nullable = false)
  private String name;

  protected MemberEntity() {
  }

  private MemberEntity(String name) {
    this.name = name;
  }

  static MemberEntity create(String name) {
    return new MemberEntity(name);
  }

  @Override
  public Long memberId() {
    return super.getId();
  }

  @Override
  public String name() {
    return this.name;
  }
}
