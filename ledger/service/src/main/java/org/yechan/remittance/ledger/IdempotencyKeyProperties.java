package org.yechan.remittance.ledger;

import java.time.Duration;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ledger.idempotency-key")
@Validated
public record IdempotencyKeyProperties(
    @NonNull Duration expiresIn
) {
}
