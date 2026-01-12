package org.yechan.remittance.account;

import java.time.LocalDateTime;

public interface ProcessedEventRepository {

  boolean existsByEventId(Long eventId);

  void markProcessed(Long eventId, LocalDateTime processedAt);
}
