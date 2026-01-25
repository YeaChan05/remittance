package org.yechan.remittance.transfer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.TransferFailedException;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

class WithdrawalRequestTest {

  @Test
  void createsValidRequest() {
    WithdrawalRequest request = new WithdrawalRequest(20L, new BigDecimal("75.50"));

    assertEquals(TransferScopeValue.WITHDRAW, request.scope());
    assertEquals(BigDecimal.ZERO, request.fee());
    assertEquals(20L, request.fromAccountId());
    assertEquals(20L, request.toAccountId());
  }

  @Test
  void rejectsNullAmount() {
    assertThrows(TransferFailedException.class,
        () -> new WithdrawalRequest(1L, null));
  }

  @Test
  void rejectsZeroOrNegativeAmount() {
    assertThrows(TransferFailedException.class,
        () -> new WithdrawalRequest(1L, BigDecimal.ZERO));
    assertThrows(TransferFailedException.class,
        () -> new WithdrawalRequest(1L, new BigDecimal("-10")));
  }

  @Test
  void rejectsNullAccountId() {
    assertThrows(TransferFailedException.class,
        () -> new WithdrawalRequest(null, BigDecimal.ONE));
  }
}
