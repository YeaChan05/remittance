package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.yechan.remittance.AuthTokenValue;

class AuthApiControllerIntegrationTest {

  @Test
  void loginReturnsTokenResponse() {
    AuthLoginUseCase authLoginUseCase = props -> new AuthTokenValue(
        "access-token",
        "refresh-token",
        1200L
    );
    var controller = new AuthApiController(authLoginUseCase);
    var request = new AuthLoginRequest("user@example.com", "password!1");

    var response = controller.login(request);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().accessToken()).isEqualTo("access-token");
    assertThat(response.getBody().refreshToken()).isEqualTo("refresh-token");
    assertThat(response.getBody().expiresIn()).isEqualTo(1200L);
  }
}
