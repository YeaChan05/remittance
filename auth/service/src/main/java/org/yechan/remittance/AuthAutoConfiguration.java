package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

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
