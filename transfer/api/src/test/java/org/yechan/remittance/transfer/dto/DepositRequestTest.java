package org.yechan.remittance.transfer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.TransferFailedException;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

class DepositRequestTest {

  @Test
  void createsValidRequest() {
    DepositRequest request = new DepositRequest(10L, new BigDecimal("50.00"));

    assertEquals(TransferScopeValue.DEPOSIT, request.scope());
    assertEquals(BigDecimal.ZERO, request.fee());
    assertEquals(10L, request.fromAccountId());
    assertEquals(10L, request.toAccountId());
  }

  @Test
  void rejectsNullAmount() {
    assertThrows(TransferFailedException.class,
        () -> new DepositRequest(1L, null));
  }

  @Test
  void rejectsZeroOrNegativeAmount() {
    assertThrows(TransferFailedException.class,
        () -> new DepositRequest(1L, BigDecimal.ZERO));
    assertThrows(TransferFailedException.class,
        () -> new DepositRequest(1L, new BigDecimal("-0.01")));
  }

  @Test
  void rejectsNullAccountId() {
    assertThrows(TransferFailedException.class,
        () -> new DepositRequest(null, BigDecimal.ONE));
  }
}
