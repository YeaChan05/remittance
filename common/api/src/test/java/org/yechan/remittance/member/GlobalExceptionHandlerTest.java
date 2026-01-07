package org.yechan.remittance.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.yechan.remittance.BusinessException;
import org.yechan.remittance.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {
  @Test
  void handleBusinessExceptionReturnsInternalServerErrorAndBody() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    BusinessException exception = new BusinessException("message");

    ResponseEntity<?> response = handler.handleBusinessException(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isSameAs(exception);
  }

  @Test
  void handleBusinessExceptionAcceptsSubclass() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    BusinessException exception = new SomeBusinessException("message");

    ResponseEntity<?> response = handler.handleBusinessException(exception);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isSameAs(exception);
  }

  private static class SomeBusinessException extends BusinessException {
    SomeBusinessException(String message) {
      super(message);
    }
  }
}
