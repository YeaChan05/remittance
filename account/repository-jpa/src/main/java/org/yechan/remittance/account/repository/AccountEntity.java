package org.yechan.remittance.account.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.yechan.remittance.BaseEntity;
import org.yechan.remittance.account.AccountModel;
import org.yechan.remittance.account.AccountProps;

@Entity
@Table(name = "account")
public class AccountEntity extends BaseEntity implements AccountModel {

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private String bankCode;

  @Column(nullable = false)
  private String accountNumber;

  @Column(nullable = false)
  private String accountName;

  protected AccountEntity() {
  }

  private AccountEntity(Long memberId, String bankCode, String accountNumber, String accountName) {
    this.memberId = memberId;
    this.bankCode = bankCode;
    this.accountNumber = accountNumber;
    this.accountName = accountName;
  }

  static AccountEntity create(AccountProps props) {
    return new AccountEntity(
        props.memberId(),
        props.bankCode(),
        props.accountNumber(),
        props.accountName()
    );
  }

  @Override
  public Long accountId() {
    return super.getId();
  }

  @Override
  public long memberId() {
    return memberId;
  }

  @Override
  public String bankCode() {
    return bankCode;
  }

  @Override
  public String accountNumber() {
    return accountNumber;
  }

  @Override
  public String accountName() {
    return accountName;
  }
}
