package org.yechan.remittance.account;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
class AccountAutoConfiguration {

  @Bean
  AccountCreateUseCase accountCreateUseCase(AccountRepository accountRepository) {
    return new AccountService(accountRepository);
  }

  @Bean
  AccountDeleteUseCase accountDeleteUseCase(AccountRepository accountRepository) {
    return new AccountDeleteService(accountRepository);
  }

  @Bean
  @ConditionalOnBean(NotificationPushPort.class)
  TransferNotificationUseCase transferNotificationUseCase(
      AccountRepository accountRepository,
      ProcessedEventRepository processedEventRepository,
      NotificationPushPort notificationPushPort
  ) {
    return new TransferNotificationService(
        accountRepository,
        processedEventRepository,
        notificationPushPort
    );
  }
}
