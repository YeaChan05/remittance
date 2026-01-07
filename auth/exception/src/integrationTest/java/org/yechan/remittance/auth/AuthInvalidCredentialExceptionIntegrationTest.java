package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthInvalidCredentialExceptionIntegrationTest {

  @Test
  void storesMessage() {
    var exception = new AuthInvalidCredentialException("invalid");

    assertThat(exception.getMessage()).isEqualTo("invalid");
  }
}
