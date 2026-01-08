package org.yechan.remittance.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class IdempotencyKeyControllerTest {

  @Test
  void createIdempotencyKeyReturnsResponse() {
    LocalDateTime expiresAt = LocalDateTime.parse("2026-01-01T01:00:00");
    IdempotencyKeyCreateUseCase useCase = props -> new TestIdempotencyKeyModel(expiresAt);

    var controller = new IdempotencyKeyController(useCase);

    var response = controller.create(1L);

    assertNotNull(response.getBody());
    assertEquals("key", response.getBody().idempotencyKey());
    assertEquals(expiresAt, response.getBody().expiresAt());
  }

  private record TestIdempotencyKeyModel(LocalDateTime expiresAt) implements IdempotencyKeyModel {

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
