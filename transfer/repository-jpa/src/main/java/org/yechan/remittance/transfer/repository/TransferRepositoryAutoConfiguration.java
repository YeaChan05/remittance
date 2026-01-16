package org.yechan.remittance.transfer.repository;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.yechan.remittance.transfer.DailyLimitUsageRepository;
import org.yechan.remittance.transfer.IdempotencyKeyRepository;
import org.yechan.remittance.transfer.LedgerRepository;
import org.yechan.remittance.transfer.OutboxEventRepository;
import org.yechan.remittance.transfer.TransferRepository;

@AutoConfiguration(before = DataJpaRepositoriesAutoConfiguration.class)
@EntityScan(basePackageClasses = {
    IdempotencyKeyEntity.class,
    TransferEntity.class,
    OutboxEventEntity.class,
    LedgerEntity.class,
    DailyLimitUsageEntity.class
})
@EnableJpaRepositories(basePackageClasses = {
    IdempotencyKeyJpaRepository.class,
    TransferJpaRepository.class,
    OutboxEventJpaRepository.class,
    LedgerJpaRepository.class,
    DailyLimitUsageJpaRepository.class
})
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

  @Bean
  LedgerRepository ledgerRepository(LedgerJpaRepository repository) {
    return new LedgerRepositoryImpl(repository);
  }

  @Bean
  DailyLimitUsageRepository dailyLimitUsageRepository(DailyLimitUsageJpaRepository repository) {
    return new DailyLimitUsageRepositoryImpl(repository);
  }
}
