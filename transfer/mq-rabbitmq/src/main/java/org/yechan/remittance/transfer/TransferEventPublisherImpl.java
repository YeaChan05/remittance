package org.yechan.remittance.transfer;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

record TransferEventPublisherImpl(
    RabbitTemplate rabbitTemplate,
    TransferEventPublisherProperties properties
) implements TransferEventPublisher {


  @Override
  public void publish(OutboxEventModel event) {
    MessagePostProcessor processor = message -> {
      message.getMessageProperties().setHeader("eventId", event.eventId());
      message.getMessageProperties().setHeader("eventType", event.eventType());
      return message;
    };

    rabbitTemplate.convertAndSend(
        properties.getExchange(),
        properties.getRoutingKey(),
        event.payload(),
        processor
    );
  }
}


