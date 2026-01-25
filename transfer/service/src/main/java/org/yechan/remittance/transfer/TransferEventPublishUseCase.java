package org.yechan.remittance.transfer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

public interface TransferEventPublishUseCase {

  int publish(Integer limit);
}

@Slf4j
record TransferEventPublishService(
    OutboxEventRepository outboxEventRepository,
    TransferEventPublisher transferEventPublisher
) implements TransferEventPublishUseCase {

  @Override
  public int publish(Integer limit) {
     log.info("transfer.event.publish.start limit={}", limit);
    List<? extends OutboxEventModel> events = outboxEventRepository.findNewForPublish(limit);
    int published = 0;
    for (OutboxEventModel event : events) {
      try {
         log.debug("transfer.event.publish.try eventId={}", event.eventId());
        transferEventPublisher.publish(event);
        outboxEventRepository.markSent(event);
        published++;
         log.info("transfer.event.publish.success eventId={}", event.eventId());
      } catch (RuntimeException ex) {
         log.error("transfer.event.publish.failed eventId={}", event.eventId(), ex);
        break;
      }
    }
     log.info("transfer.event.publish.done published={}", published);
    return published;
  }
}
