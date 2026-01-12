package org.yechan.remittance.transfer.dto;

import static org.yechan.remittance.transfer.TransferFailureCode.INVALID_REQUEST;

import java.math.BigDecimal;
import org.yechan.remittance.transfer.TransferFailedException;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;
import org.yechan.remittance.transfer.TransferRequestProps;

public record DepositRequest(
    Long accountId,
    BigDecimal amount
) implements TransferRequestProps {

  public DepositRequest {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new TransferFailedException(INVALID_REQUEST, "Invalid amount");
    }
    if (accountId == null) {
      throw new TransferFailedException(INVALID_REQUEST, "Account ID must not be null");
    }
  }

  @Override
  public Long fromAccountId() {
    return accountId;
  }

  @Override
  public Long toAccountId() {
    return accountId;
  }

  @Override
  public TransferScopeValue scope() {
    return TransferScopeValue.DEPOSIT;
  }

  @Override
  public BigDecimal fee() {
    return BigDecimal.ZERO;
  }
}
