package org.yechan.remittance.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyKeyStatusValue;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

class TransferIdempotencyHandlerTest {

  @Test
  void loadKeyThrowsWhenExpired() {
    LocalDateTime now = LocalDateTime.parse("2026-01-01T10:00:00");
    TestKey key = new TestKey(IdempotencyKeyStatusValue.BEFORE_START,
        LocalDateTime.parse("2026-01-01T09:00:00"), null);
    TransferIdempotencyHandler handler = new TransferIdempotencyHandler(
        new FixedKeyRepository(key),
        new TransferSnapshotUtil(new ObjectMapper())
    );

    assertThrows(TransferIdempotencyKeyExpiredException.class,
        () -> handler.loadKey(1L, "k", IdempotencyScopeValue.TRANSFER, now));
  }

  @Test
  void resolveExistingThrowsOnRequestHashConflict() {
    TestKey key = new TestKey(IdempotencyKeyStatusValue.BEFORE_START, null, null);
    key.requestHash = "hash";
    TransferIdempotencyHandler handler = new TransferIdempotencyHandler(
        new FixedKeyRepository(key),
        new TransferSnapshotUtil(new ObjectMapper())
    );

    assertThrows(TransferIdempotencyKeyConflictException.class,
        () -> handler.resolveExisting(1L, "k", IdempotencyScopeValue.TRANSFER, "other"));
  }

  @Test
  void resolveExistingReturnsInProgressForInProgressStatus() {
    TestKey key = new TestKey(IdempotencyKeyStatusValue.IN_PROGRESS, null, null);
    TransferIdempotencyHandler handler = new TransferIdempotencyHandler(
        new FixedKeyRepository(key),
        new TransferSnapshotUtil(new ObjectMapper())
    );

    TransferResult result = handler.resolveExisting(1L, "k", IdempotencyScopeValue.TRANSFER, null);

    assertEquals(TransferStatusValue.IN_PROGRESS, result.status());
  }

  @Test
  void resolveExistingReturnsInProgressWhenSnapshotMissing() {
    TestKey key = new TestKey(IdempotencyKeyStatusValue.SUCCEEDED, null, null);
    TransferIdempotencyHandler handler = new TransferIdempotencyHandler(
        new FixedKeyRepository(key),
        new TransferSnapshotUtil(new ObjectMapper())
    );

    TransferResult result = handler.resolveExisting(1L, "k", IdempotencyScopeValue.TRANSFER, null);

    assertEquals(TransferStatusValue.IN_PROGRESS, result.status());
  }

  @Test
  void resolveExistingReturnsSnapshot() {
    TransferSnapshotUtil snapshotUtil = new TransferSnapshotUtil(new ObjectMapper());
    TransferResult snapshotResult = new TransferResult(TransferStatusValue.SUCCEEDED, 99L, null);
    String snapshot = snapshotUtil.toSnapshot(snapshotResult);
    TestKey key = new TestKey(IdempotencyKeyStatusValue.SUCCEEDED, null, snapshot);

    TransferIdempotencyHandler handler = new TransferIdempotencyHandler(
        new FixedKeyRepository(key),
        snapshotUtil
    );

    TransferResult result = handler.resolveExisting(1L, "k", IdempotencyScopeValue.TRANSFER, null);

    assertEquals(snapshotResult.status(), result.status());
    assertEquals(snapshotResult.transferId(), result.transferId());
  }

  private static class FixedKeyRepository implements IdempotencyKeyRepository {

    private final IdempotencyKeyModel key;

    private FixedKeyRepository(IdempotencyKeyModel key) {
      this.key = key;
    }

    @Override
    public IdempotencyKeyModel save(IdempotencyKeyProps props) {
      return key;
    }

    @Override
    public Optional<IdempotencyKeyModel> findByKey(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey
    ) {
      return Optional.ofNullable(key);
    }

    @Override
    public boolean tryMarkInProgress(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey,
        String requestHash,
        LocalDateTime startedAt
    ) {
      return false;
    }

    @Override
    public IdempotencyKeyModel markSucceeded(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey,
        String responseSnapshot,
        LocalDateTime completedAt
    ) {
      return key;
    }

    @Override
    public IdempotencyKeyModel markFailed(
        Long memberId,
        IdempotencyScopeValue scope,
        String idempotencyKey,
        String responseSnapshot,
        LocalDateTime completedAt
    ) {
      return key;
    }

    @Override
    public int markTimeoutBefore(LocalDateTime cutoff, String responseSnapshot) {
      return 0;
    }
  }

  private static class TestKey implements IdempotencyKeyModel {

    private final IdempotencyKeyStatusValue status;
    private final LocalDateTime expiresAt;
    private final String responseSnapshot;
    private String requestHash;

    private TestKey(
        IdempotencyKeyStatusValue status,
        LocalDateTime expiresAt,
        String responseSnapshot
    ) {
      this.status = status;
      this.expiresAt = expiresAt;
      this.responseSnapshot = responseSnapshot;
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
      return "k";
    }

    @Override
    public LocalDateTime expiresAt() {
      return expiresAt;
    }

    @Override
    public IdempotencyScopeValue scope() {
      return IdempotencyScopeValue.TRANSFER;
    }

    @Override
    public IdempotencyKeyStatusValue status() {
      return status;
    }

    @Override
    public String requestHash() {
      return requestHash;
    }

    @Override
    public String responseSnapshot() {
      return responseSnapshot;
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
