package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.yechan.remittance.AuthTokenValue;
import org.yechan.remittance.TokenGenerator;

class AuthServiceIntegrationTest {

  @Test
  void loginReturnsTokenWhenCredentialsAreValid() {
    MemberAuthClient memberAuthClient = (email, password) -> new MemberAuthResult(true, 1L);
    TokenGenerator tokenGenerator = memberId -> new AuthTokenValue("access", "refresh", 3600L);
    AuthLoginUseCase useCase = new AuthService(memberAuthClient, tokenGenerator);

    var token = useCase.login(new TestAuthLoginProps());

    assertThat(token.accessToken()).isEqualTo("access");
    assertThat(token.refreshToken()).isEqualTo("refresh");
    assertThat(token.expiresIn()).isEqualTo(3600L);
  }

  @Test
  void loginThrowsWhenCredentialsAreInvalid() {
    MemberAuthClient memberAuthClient = (email, password) -> new MemberAuthResult(false, 1L);
    TokenGenerator tokenGenerator = memberId -> new AuthTokenValue("access", "refresh", 3600L);
    AuthLoginUseCase useCase = new AuthService(memberAuthClient, tokenGenerator);

    assertThatThrownBy(() -> useCase.login(new TestAuthLoginProps()))
        .isInstanceOf(AuthInvalidCredentialException.class)
        .hasMessage("Invalid credentials");
  }

  private static class TestAuthLoginProps implements AuthLoginProps {

    @Override
    public String email() {
      return "user@example.com";
    }

    @Override
    public String password() {
      return "password!1";
    }
  }
}
