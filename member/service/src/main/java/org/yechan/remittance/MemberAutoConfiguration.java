package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
class MemberAutoConfiguration {
  @Bean
  MemberCreateUseCase memberCreateUseCase(MemberRepository memberRepository) {
    return new MemberService(memberRepository);
  }
}
