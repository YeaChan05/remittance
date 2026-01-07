package org.yechan.remittance.member;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
class MemberInternalApiAutoConfiguration {

  @Bean
  MemberInternalApi memberInternalApi(MemberAuthQueryUseCase memberAuthQueryUseCase) {
    return new MemberInternalAdapter(memberAuthQueryUseCase);
  }
}
