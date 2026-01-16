package org.yechan.remittance.transfer;

import java.math.BigDecimal;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public interface TransferRequestProps {

  Long fromAccountId();

  Long toAccountId();

  BigDecimal amount();

  TransferScopeValue scope();

  BigDecimal fee();

  default IdempotencyScopeValue toIdempotencyScope(
  ) {
    return switch (this.scope()) {
      case WITHDRAW -> IdempotencyScopeValue.WITHDRAW;
      case DEPOSIT -> IdempotencyScopeValue.DEPOSIT;
      default -> IdempotencyScopeValue.TRANSFER;
    };
  }

  default BigDecimal debit() {
    return amount().add(fee());
  }

  ;
}
