package org.yechan.remittance.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class IdempotencyKeyControllerTest {

  @Test
  void createIdempotencyKeyReturnsResponse() {
    Instant expiresAt = Instant.parse("2024-01-01T01:00:00Z");
    IdempotencyKeyCreateUseCase useCase = props -> new IdempotencyKey(
        1L,
        props.memberId(),
        "key",
        expiresAt
    );
    var controller = new IdempotencyKeyController(useCase);

    var response = controller.create(1L);

    assertNotNull(response.getBody());
    assertEquals("key", response.getBody().idempotencyKey());
    assertEquals(expiresAt, response.getBody().expiresAt());
  }
}
