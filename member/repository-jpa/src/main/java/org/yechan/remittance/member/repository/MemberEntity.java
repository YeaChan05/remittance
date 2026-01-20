package org.yechan.remittance.member.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.member.MemberModel;
import org.yechan.remittance.member.MemberProps;

@Entity
@Table(name = "member", catalog = "core")
public class MemberEntity extends BaseEntity implements MemberModel {

  @Column(nullable = false)
  private String name;
  @Column(nullable = false, unique = true)
  private String email;
  @Column(nullable = false)
  private String passwordHash;

  protected MemberEntity() {
  }

  private MemberEntity(String name, String email, String passwordHash) {
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  static MemberEntity create(MemberProps props) {
    return new MemberEntity(
        props.name(),
        props.email(),
        props.password()
    );
  }

  @Override
  public Long memberId() {
    return super.getId();
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public String email() {
    return this.email;
  }

  @Override
  public String password() {
    return this.passwordHash;
  }
}
