package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.yechan.remittance.AuthTokenValue;
import org.yechan.remittance.TokenGenerator;

class AuthAutoConfigurationTest {

  @Test
  void authLoginUseCaseBeanUsesDependencies() {
    MemberAuthClient memberAuthClient = (email, password) -> new MemberAuthResult(true, 2L);
    TokenGenerator tokenGenerator = memberId -> new AuthTokenValue("token", "refresh", 100L);
    var configuration = new AuthAutoConfiguration();

    var useCase = configuration.authLoginUseCase(memberAuthClient, tokenGenerator);

    var token = useCase.login(new AuthLoginProps() {
      @Override
      public String email() {
        return "user@example.com";
      }

      @Override
      public String password() {
        return "password!1";
      }
    });

    assertThat(token.accessToken()).isEqualTo("token");
    assertThat(token.refreshToken()).isEqualTo("refresh");
    assertThat(token.expiresIn()).isEqualTo(100L);
  }
}
