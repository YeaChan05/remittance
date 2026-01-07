package org.yechan.remittance.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.yechan.remittance.TokenGenerator;

@AutoConfiguration
class AuthAutoConfiguration {

  @Bean
  AuthLoginUseCase authLoginUseCase(
      MemberAuthClient memberAuthClient,
      TokenGenerator tokenGenerator
  ) {
    return new AuthService(memberAuthClient, tokenGenerator);
  }
}
