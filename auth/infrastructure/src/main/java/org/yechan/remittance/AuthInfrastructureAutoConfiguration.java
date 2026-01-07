package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
class AuthInfrastructureAutoConfiguration {

  @Bean
  MemberAuthClient memberAuthClient(MemberInternalApi memberInternalApi) {
    return new MemberAuthClientAdapter(memberInternalApi);
  }
}
