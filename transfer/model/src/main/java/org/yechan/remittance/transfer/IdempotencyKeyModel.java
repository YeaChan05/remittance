package org.yechan.remittance.transfer;

import java.time.LocalDateTime;

public interface IdempotencyKeyModel extends IdempotencyKeyProps, IdempotencyKeyIdentifier {

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
