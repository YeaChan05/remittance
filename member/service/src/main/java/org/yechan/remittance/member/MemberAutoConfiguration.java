package org.yechan.remittance.member;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.yechan.remittance.PasswordHashEncoder;
import org.yechan.remittance.TokenGenerator;

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

  @Bean
  MemberAuthQueryUseCase memberAuthQueryUseCase(
      MemberRepository memberRepository,
      PasswordHashEncoder passwordHashEncoder) {
    return new MemberAuthQueryService(memberRepository, passwordHashEncoder);
  }

}
