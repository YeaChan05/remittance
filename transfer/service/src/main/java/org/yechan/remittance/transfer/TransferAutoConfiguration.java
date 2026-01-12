package org.yechan.remittance.transfer;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.yechan.remittance.account.AccountRepository;

@AutoConfiguration
class TransferAutoConfiguration {

  @Bean
  TransferIdempotencyHandler transferIdempotencyHandler(
      IdempotencyKeyRepository idempotencyKeyRepository
  ) {
    return new TransferIdempotencyHandler(idempotencyKeyRepository);
  }

  @Bean
  TransferProcessService transferProcessService(
      AccountRepository accountRepository,
      TransferRepository transferRepository,
      OutboxEventRepository outboxEventRepository,
      IdempotencyKeyRepository idempotencyKeyRepository
  ) {
    return new TransferProcessService(
        accountRepository,
        transferRepository,
        outboxEventRepository,
        idempotencyKeyRepository
    );
  }

  @Bean
  LedgerWriter ledgerWriter(LedgerRepository ledgerRepository) {
    return new LedgerWriter(ledgerRepository);
  }

  @Bean
  TransferQueryUseCase transferQueryUseCase(
      AccountRepository accountRepository,
      TransferRepository transferRepository
  ) {
    return new TransferQueryService(accountRepository, transferRepository);
  }

  @Bean
  TransferCreateUseCase transferCreateUseCase(
      TransferIdempotencyHandler idempotencyHandler,
      TransferProcessService transferProcessService,
      LedgerWriter ledgerWriter,
      Clock clock
  ) {
    return new TransferService(
        idempotencyHandler,
        transferProcessService,
        ledgerWriter,
        clock
    );
  }

  @Bean
  @ConditionalOnBean(TransferEventPublisher.class)
  TransferEventPublishUseCase transferEventPublishUseCase(
      OutboxEventRepository outboxEventRepository,
      TransferEventPublisher transferEventPublisher
  ) {
    return new TransferEventPublishService(outboxEventRepository, transferEventPublisher);
  }
}
