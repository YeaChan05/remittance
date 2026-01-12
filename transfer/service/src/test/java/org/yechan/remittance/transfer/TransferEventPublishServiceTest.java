package org.yechan.remittance.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransferEventPublishServiceTest {

  @Test
  void publishMarksEventsAsSent() {
    var repository = new FakeOutboxEventRepository(sampleEvents());
    var publisher = new FakeTransferEventPublisher();
    var service = new TransferEventPublishService(repository, publisher);

    int published = service.publish(10);

    assertEquals(2, published);
    assertEquals(List.of(1L, 2L), repository.sentEventIds());
    assertEquals(2, publisher.publishedCount());
  }

  @Test
  void publishStopsWhenPublisherFails() {
    var repository = new FakeOutboxEventRepository(sampleEvents());
    var publisher = new FakeTransferEventPublisher();
    publisher.failOn(2L);
    var service = new TransferEventPublishService(repository, publisher);

    int published = service.publish(10);

    assertEquals(1, published);
    assertEquals(List.of(1L), repository.sentEventIds());
    assertEquals(1, publisher.publishedCount());
  }

  private List<OutboxEvent> sampleEvents() {
    return List.of(
        new OutboxEvent(1L, "TRANSFER", "1", "TRANSFER_COMPLETED", "payload", status(), now()),
        new OutboxEvent(2L, "TRANSFER", "2", "TRANSFER_COMPLETED", "payload", status(), now())
    );
  }

  private OutboxEventProps.OutboxEventStatusValue status() {
    return OutboxEventProps.OutboxEventStatusValue.NEW;
  }

  private LocalDateTime now() {
    return LocalDateTime.of(2025, 1, 1, 0, 0);
  }

  private static class FakeOutboxEventRepository implements OutboxEventRepository {

    private final List<OutboxEvent> events;
    private final List<Long> sentEventIds = new ArrayList<>();

    private FakeOutboxEventRepository(List<OutboxEvent> events) {
      this.events = events;
    }

    @Override
    public OutboxEventModel save(OutboxEventProps props) {
      throw new UnsupportedOperationException("Not needed");
    }

    @Override
    public List<? extends OutboxEventModel> findNewForPublish(Integer limit) {
      return events;
    }

    @Override
    public void markSent(OutboxEventIdentifier identifier) {
      sentEventIds.add(identifier.eventId());
    }

    private List<Long> sentEventIds() {
      return sentEventIds;
    }
  }

  private static class FakeTransferEventPublisher implements TransferEventPublisher {

    private final List<Long> published = new ArrayList<>();
    private Long failOnEventId;

    @Override
    public void publish(OutboxEventModel event) {
      if (event.eventId().equals(failOnEventId)) {
        throw new IllegalStateException("publish failed");
      }
      published.add(event.eventId());
    }

    private void failOn(Long eventId) {
      this.failOnEventId = eventId;
    }

    private int publishedCount() {
      return published.size();
    }
  }
}
