package org.yechan.remittance.transfer.repository;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.yechan.remittance.transfer.IdempotencyKeyRepository;
import org.yechan.remittance.transfer.OutboxEventRepository;
import org.yechan.remittance.transfer.TransferRepository;

@AutoConfiguration(before = DataJpaRepositoriesAutoConfiguration.class)
@EntityScan(basePackageClasses = {
    IdempotencyKeyEntity.class,
    TransferEntity.class,
    OutboxEventEntity.class
})
@EnableJpaRepositories(basePackageClasses = IdempotencyKeyJpaRepository.class)
public class TransferRepositoryAutoConfiguration {

  @Bean
  IdempotencyKeyRepository idempotencyKeyRepository(IdempotencyKeyJpaRepository repository) {
    return new IdempotencyKeyRepositoryImpl(repository);
  }

  @Bean
  TransferRepository transferRepository(TransferJpaRepository repository) {
    return new TransferRepositoryImpl(repository);
  }

  @Bean
  OutboxEventRepository outboxEventRepository(OutboxEventJpaRepository repository) {
    return new OutboxEventRepositoryImpl(repository);
  }
}
