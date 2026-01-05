package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration(before = DataJpaRepositoriesAutoConfiguration.class)
@EntityScan(basePackageClasses = MemberEntity.class)
@EnableJpaRepositories(basePackageClasses = MemberJpaRepository.class)
public class MemberRepositoryAutoConfiguration {

  @Bean
  MemberRepository memberRepository(MemberJpaRepository repository) {
    return new MemberRepositoryImpl(repository);
  }
}
