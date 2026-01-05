package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EntityScan(basePackageClasses = MemberEntity.class)
public class MemberAutoConfiguration {
  @Bean
  MemberRepository memberRepository(MemberJpaRepository repository) {
    return new MemberRepositoryImpl(repository);
  }
}
