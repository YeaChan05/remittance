package org.yechan.remittance.transfer.dto;

import static org.yechan.remittance.transfer.TransferFailureCode.INVALID_REQUEST;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.yechan.remittance.transfer.TransferFailedException;
import org.yechan.remittance.transfer.TransferRequestProps;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public record TransferRequest(
    Long fromAccountId,
    Long toAccountId,
    BigDecimal amount
) implements TransferRequestProps {

  private static final BigDecimal FEE_RATE = new BigDecimal("0.01");

  public TransferRequest {
    if (amount == null
        || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new TransferFailedException(INVALID_REQUEST, "Invalid amount");
    }
    if (fromAccountId == null || toAccountId == null) {
      throw new TransferFailedException(INVALID_REQUEST, "Account IDs must not be null");
    }
  }

  @Override
  public TransferScopeValue scope() {
    return TransferScopeValue.TRANSFER;
  }

  @Override
  public BigDecimal fee() {
    return amount.multiply(FEE_RATE).setScale(2, RoundingMode.DOWN);
  }
}
