package org.yechan.remittance.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class TransferNotificationConsumerTest {

  @Test
  void consumeDelegatesToUseCase() {
    TransferNotificationUseCase useCase = mock(TransferNotificationUseCase.class);
    var parser = new TransferNotificationPayloadParser();
    var consumer = new TransferNotificationConsumer(useCase, parser);
    String payload = """
        {"transferId":11,"fromAccountId":1,"toAccountId":2,"amount":10000,"completedAt":"2025-01-01T00:00"}
        """;

    consumer.consume(payload, 22L, "TRANSFER_COMPLETED");

    verify(useCase).notify(any(TransferNotificationProps.class));
  }

  @Test
  void consumeIgnoresOtherEventTypes() {
    TransferNotificationUseCase useCase = mock(TransferNotificationUseCase.class);
    var parser = new TransferNotificationPayloadParser();
    var consumer = new TransferNotificationConsumer(useCase, parser);
    String payload = """
        {"transferId":11,"fromAccountId":1,"toAccountId":2,"amount":10000,"completedAt":"2025-01-01T00:00"}
        """;

    consumer.consume(payload, 22L, "OTHER_EVENT");

    verify(useCase, never()).notify(any(TransferNotificationProps.class));
  }

  @Test
  void consumeIgnoresMissingEventId() {
    TransferNotificationUseCase useCase = mock(TransferNotificationUseCase.class);
    var parser = new TransferNotificationPayloadParser();
    var consumer = new TransferNotificationConsumer(useCase, parser);
    String payload = """
        {"transferId":11,"fromAccountId":1,"toAccountId":2,"amount":10000,"completedAt":"2025-01-01T00:00"}
        """;

    consumer.consume(payload, null, "TRANSFER_COMPLETED");

    verify(useCase, never()).notify(any(TransferNotificationProps.class));
  }
}
