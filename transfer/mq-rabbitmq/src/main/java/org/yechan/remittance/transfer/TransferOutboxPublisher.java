package org.yechan.remittance.transfer;

import org.springframework.scheduling.annotation.Scheduled;

class TransferOutboxPublisher {

  private final TransferEventPublishUseCase transferEventPublishUseCase;
  private final TransferOutboxProperties properties;

  TransferOutboxPublisher(
      TransferEventPublishUseCase transferEventPublishUseCase,
      TransferOutboxProperties properties
  ) {
    this.transferEventPublishUseCase = transferEventPublishUseCase;
    this.properties = properties;
  }

  @Scheduled(fixedDelayString = "${transfer.outbox.publish-delay-ms:1000}")
  public void publish() {
    transferEventPublishUseCase.publish(properties.getBatchSize());
  }
}
