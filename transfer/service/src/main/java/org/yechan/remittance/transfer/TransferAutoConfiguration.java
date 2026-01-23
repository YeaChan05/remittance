package org.yechan.remittance.transfer;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.yechan.remittance.account.AccountRepository;
import org.yechan.remittance.member.MemberRepository;

@AutoConfiguration
class TransferAutoConfiguration {

  @Bean
  TransferIdempotencyHandler transferIdempotencyHandler(
      IdempotencyKeyRepository idempotencyKeyRepository,
      TransferSnapshotUtil transferSnapshotUtil
  ) {
    return new TransferIdempotencyHandler(idempotencyKeyRepository, transferSnapshotUtil);
  }

  @Bean
  TransferProcessService transferProcessService(
      AccountRepository accountRepository,
      TransferRepository transferRepository,
      OutboxEventRepository outboxEventRepository,
      IdempotencyKeyRepository idempotencyKeyRepository,
      DailyLimitUsageRepository dailyLimitUsageRepository,
      MemberRepository memberRepository,
      TransferSnapshotUtil transferSnapshotUtil
  ) {
    return new TransferProcessService(
        accountRepository,
        transferRepository,
        outboxEventRepository,
        idempotencyKeyRepository,
        dailyLimitUsageRepository,
        memberRepository,
        transferSnapshotUtil
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
      TransferSnapshotUtil transferSnapshotUtil,
      Clock clock
  ) {
    return new TransferService(
        idempotencyHandler,
        transferProcessService,
        ledgerWriter,
        transferSnapshotUtil,
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
