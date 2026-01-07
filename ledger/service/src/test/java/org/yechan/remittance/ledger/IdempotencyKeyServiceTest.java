package org.yechan.remittance.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class IdempotencyKeyServiceTest {

  @Test
  void createsIdempotencyKeyWithExpiration() {
    var saved = new AtomicReference<IdempotencyKeyProps>();
    IdempotencyKeyRepository repository = props -> {
      saved.set(props);
      return new IdempotencyKey(saved.get().memberId(), props.memberId(), props.idempotencyKey(),
          props.expiresAt());
    };
    Instant now = Instant.parse("2024-01-01T00:00:00Z");
    Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    var properties = new IdempotencyKeyProperties(Duration.ofHours(1));
    var service = new IdempotencyKeyService(repository, clock, properties);

    IdempotencyKeyModel created = service.create(() -> 10L);

    assertThat(created.memberId()).isEqualTo(10L);
    assertThat(created.idempotencyKey()).isNotBlank();
    assertThat(created.expiresAt()).isEqualTo(now.plus(Duration.ofHours(1)));
    assertThat(saved.get()).isNotNull();
    assertThat(saved.get().idempotencyKey()).isEqualTo(created.idempotencyKey());
  }
}
