package org.yechan.remittance.transfer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(prefix = "transfer.outbox", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties({
    TransferOutboxProperties.class,
    TransferEventPublisherProperties.class
})
class TransferOutboxAutoConfiguration {

  @Bean
  TransferEventPublisher transferEventPublisher(
      RabbitTemplate rabbitTemplate,
      TransferEventPublisherProperties properties
  ) {
    return new TransferEventPublisherImpl(rabbitTemplate, properties);
  }

  @Bean
  @ConditionalOnBean(TransferEventPublishUseCase.class)
  TransferOutboxPublisher transferOutboxPublisher(
      TransferEventPublishUseCase transferEventPublishUseCase,
      TransferOutboxProperties properties
  ) {
    return new TransferOutboxPublisher(transferEventPublishUseCase, properties);
  }
}
