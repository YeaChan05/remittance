package org.yechan.remittance.ledger;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(IdempotencyKeyProperties.class)
class IdempotencyKeyAutoConfiguration {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  IdempotencyKeyCreateUseCase idempotencyKeyCreateUseCase(
      IdempotencyKeyRepository repository,
      Clock idempotencyKeyClock,
      IdempotencyKeyProperties properties
  ) {
    return new IdempotencyKeyService(repository, idempotencyKeyClock, properties);
  }
}
