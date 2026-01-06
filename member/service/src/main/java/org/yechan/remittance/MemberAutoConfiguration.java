package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
class MemberAutoConfiguration {

  @Bean
  MemberCreateUseCase memberCreateUseCase(
      MemberRepository memberRepository,
      PasswordHashEncoder passwordHashEncoder) {
    return new MemberService(memberRepository, passwordHashEncoder);
  }

  @Bean
  MemberQueryUseCase memberQueryUseCase(
      MemberRepository memberRepository,
      PasswordHashEncoder passwordHashEncoder,
      TokenGenerator tokenGenerator) {
    return new MemberQueryService(memberRepository, passwordHashEncoder, tokenGenerator);
  }

}
