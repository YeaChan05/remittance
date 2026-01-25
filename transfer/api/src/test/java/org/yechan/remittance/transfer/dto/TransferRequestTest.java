package org.yechan.remittance.transfer.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.TransferFailedException;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

class TransferRequestTest {

  @Test
  void createsValidRequest() {
    TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.129"));

    assertEquals(TransferScopeValue.TRANSFER, request.scope());
    assertEquals(new BigDecimal("1.00"), request.fee());
  }

  @Test
  void rejectsNullAmount() {
    assertThrows(TransferFailedException.class,
        () -> new TransferRequest(1L, 2L, null));
  }

  @Test
  void rejectsZeroOrNegativeAmount() {
    assertThrows(TransferFailedException.class,
        () -> new TransferRequest(1L, 2L, BigDecimal.ZERO));
    assertThrows(TransferFailedException.class,
        () -> new TransferRequest(1L, 2L, new BigDecimal("-1")));
  }

  @Test
  void rejectsNullAccountIds() {
    assertThrows(TransferFailedException.class,
        () -> new TransferRequest(null, 2L, BigDecimal.ONE));
    assertThrows(TransferFailedException.class,
        () -> new TransferRequest(1L, null, BigDecimal.ONE));
  }
}
