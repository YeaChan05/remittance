package org.yechan.remittance.transfer.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.IdempotencyKeyProps;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;

class IdempotencyKeyEntityTest {

  @Test
  void createInitializesDefaults() {
    IdempotencyKeyEntity entity = IdempotencyKeyEntity.create(new TestProps());

    assertEquals(IdempotencyKeyStatusValue.BEFORE_START, entity.status());
    assertNull(entity.requestHash());
    assertNull(entity.responseSnapshot());
  }

  @Test
  void tryMarkInProgressSucceedsOnlyOnce() {
    IdempotencyKeyEntity entity = IdempotencyKeyEntity.create(new TestProps());
    LocalDateTime startedAt = LocalDateTime.parse("2026-01-01T10:00:00");

    assertTrue(entity.tryMarkInProgress("hash", startedAt));
    assertEquals(IdempotencyKeyStatusValue.IN_PROGRESS, entity.status());
    assertEquals("hash", entity.requestHash());
    assertEquals(startedAt, entity.startedAt());
    assertFalse(entity.tryMarkInProgress("hash2", startedAt));
  }

  @Test
  void markTimeoutIfBeforeRequiresInProgressAndPastStart() {
    IdempotencyKeyEntity entity = IdempotencyKeyEntity.create(new TestProps());
    LocalDateTime cutoff = LocalDateTime.parse("2026-01-01T10:00:00");

    entity.markSucceeded("snapshot", cutoff);
    assertFalse(entity.markTimeoutIfBefore(cutoff, "timeout", cutoff));

    IdempotencyKeyEntity inProgressNoStart = IdempotencyKeyEntity.create(new TestProps());
    inProgressNoStart.tryMarkInProgress("hash", null);
    assertFalse(inProgressNoStart.markTimeoutIfBefore(cutoff, "timeout", cutoff));

    IdempotencyKeyEntity inProgressAfterCutoff = IdempotencyKeyEntity.create(new TestProps());
    inProgressAfterCutoff.tryMarkInProgress("hash", cutoff);
    assertFalse(inProgressAfterCutoff.markTimeoutIfBefore(cutoff, "timeout", cutoff));
  }

  @Test
  void markTimeoutIfBeforeMarksTimeout() {
    IdempotencyKeyEntity entity = IdempotencyKeyEntity.create(new TestProps());
    LocalDateTime startedAt = LocalDateTime.parse("2026-01-01T09:00:00");
    LocalDateTime cutoff = LocalDateTime.parse("2026-01-01T10:00:00");
    LocalDateTime completedAt = LocalDateTime.parse("2026-01-01T10:30:00");

    entity.tryMarkInProgress("hash", startedAt);

    assertTrue(entity.markTimeoutIfBefore(cutoff, "timeout", completedAt));
    assertEquals(IdempotencyKeyStatusValue.TIMEOUT, entity.status());
    assertEquals("timeout", entity.responseSnapshot());
    assertEquals(completedAt, entity.completedAt());
  }

  private record TestProps() implements IdempotencyKeyProps {

    @Override
    public Long memberId() {
      return 1L;
    }

    @Override
    public String idempotencyKey() {
      return "key";
    }

    @Override
    public LocalDateTime expiresAt() {
      return LocalDateTime.parse("2026-01-02T00:00:00");
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
    public String requestHash() {
      return null;
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
