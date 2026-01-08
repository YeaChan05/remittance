package org.yechan.remittance.transfer;

import java.time.Instant;

public interface OutboxEventProps {

  String aggregateType();

  String aggregateId();

  String eventType();

  String payload();

  OutboxEventStatusValue status();

  Instant createdAt();

  enum OutboxEventStatusValue {
    NEW,
    SENT
  }
}
