package org.yechan.remittance.transfer;

public interface TransferEventPublisher {

  void publish(OutboxEventModel event);
}
