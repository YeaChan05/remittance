package org.yechan.remittance.transfer;

import java.time.LocalDateTime;

public interface IdempotencyKeyModel extends IdempotencyKeyProps, IdempotencyKeyIdentifier {

  default boolean tryMarkInProgress(String requestHash, LocalDateTime startedAt) {
    throw new UnsupportedOperationException("Try mark in progress not supported");
  }

  default void markSucceeded(String responseSnapshot, LocalDateTime completedAt) {
    throw new UnsupportedOperationException("Mark succeeded not supported");
  }

  default void markFailed(String responseSnapshot, LocalDateTime completedAt) {
    throw new UnsupportedOperationException("Mark failed not supported");
  }

  default boolean markTimeoutIfBefore(
      LocalDateTime cutoff,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    throw new UnsupportedOperationException("Mark timeout not supported");
  }

  default boolean isInvalidRequestHash(String requestHash) {
    if (requestHash() == null || requestHash == null) {
      return false;
    }
    return !requestHash().equals(requestHash);
  }

  default boolean isExpired(LocalDateTime now) {
    return expiresAt() != null && expiresAt().isBefore(now);
  }
}
