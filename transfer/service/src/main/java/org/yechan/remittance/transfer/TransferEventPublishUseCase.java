package org.yechan.remittance.transfer;

import java.util.List;

public interface TransferEventPublishUseCase {

  int publish(Integer limit);
}

record TransferEventPublishService(
    OutboxEventRepository outboxEventRepository,
    TransferEventPublisher transferEventPublisher
) implements TransferEventPublishUseCase {

  @Override
  public int publish(Integer limit) {
    List<? extends OutboxEventModel> events = outboxEventRepository.findNewForPublish(limit);
    int published = 0;
    for (OutboxEventModel event : events) {
      try {
        transferEventPublisher.publish(event);
        outboxEventRepository.markSent(event);
        published++;
      } catch (RuntimeException ex) {
        break;
      }
    }
    return published;
  }
}
