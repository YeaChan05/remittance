package org.yechan.remittance.transfer;

import java.time.LocalDateTime;

public record OutboxEvent(
    Long eventId,
    String aggregateType,
    String aggregateId,
    String eventType,
    String payload,
    OutboxEventStatusValue status,
    LocalDateTime createdAt
) implements OutboxEventModel {

}
