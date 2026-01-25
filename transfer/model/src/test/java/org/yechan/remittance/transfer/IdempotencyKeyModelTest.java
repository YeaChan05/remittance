package org.yechan.remittance.transfer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class IdempotencyKeyModelTest {

  @Test
  void detectsInvalidRequestHash() {
    IdempotencyKeyModel key = new TestKey("hash");

    assertFalse(key.isInvalidRequestHash(null));
    assertFalse(new TestKey(null).isInvalidRequestHash("hash"));
    assertTrue(key.isInvalidRequestHash("other"));
    assertFalse(key.isInvalidRequestHash("hash"));
  }

  @Test
  void detectsExpiration() {
    LocalDateTime now = LocalDateTime.parse("2026-01-01T10:00:00");

    assertFalse(new TestKey(null, null).isExpired(now));
    assertFalse(new TestKey(null, now.plusMinutes(1)).isExpired(now));
    assertTrue(new TestKey(null, now.minusMinutes(1)).isExpired(now));
  }

  private record TestKey(String requestHash, LocalDateTime expiresAt) implements
      IdempotencyKeyModel {

    TestKey(String requestHash) {
      this(requestHash, LocalDateTime.parse("2026-01-01T09:00:00"));
    }

    @Override
    public Long idempotencyKeyId() {
      return 1L;
    }

    @Override
    public Long memberId() {
      return 1L;
    }

    @Override
    public String idempotencyKey() {
      return "key";
    }

    @Override
    public IdempotencyScopeValue scope() {
      return IdempotencyScopeValue.TRANSFER;
    }

    @Override
    public IdempotencyKeyStatusValue status() {
      return IdempotencyKeyStatusValue.BEFORE_START;
    }

    @Override
    public String responseSnapshot() {
      return null;
    }

    @Override
    public LocalDateTime startedAt() {
      return null;
    }

    @Override
    public LocalDateTime completedAt() {
      return null;
    }
  }
}
