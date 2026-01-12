package org.yechan.remittance.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class TransferEventPublisherImplTest {

  @Test
  void publishSetsHeadersAndPayload() {
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    var properties = new TransferEventPublisherProperties();
    properties.setExchange("transfer.exchange");
    properties.setRoutingKey("transfer.completed");
    var publisher = new TransferEventPublisherImpl(rabbitTemplate, properties);
    var event = new OutboxEvent(
        11L,
        "TRANSFER",
        "1",
        "TRANSFER_COMPLETED",
        "payload",
        OutboxEventProps.OutboxEventStatusValue.NEW,
        LocalDateTime.of(2025, 1, 1, 0, 0)
    );

    publisher.publish(event);

    ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
    verify(rabbitTemplate)
        .convertAndSend(eq("transfer.exchange"), eq("transfer.completed"), eq("payload"),
            captor.capture());

    Message message = new Message("payload".getBytes(StandardCharsets.UTF_8),
        new MessageProperties());
    captor.getValue().postProcessMessage(message);

    assertEquals(11L, message.getMessageProperties().getHeaders().get("eventId"));
    assertEquals("TRANSFER_COMPLETED",
        message.getMessageProperties().getHeaders().get("eventType"));
  }
}
