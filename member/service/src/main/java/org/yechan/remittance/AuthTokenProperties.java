package org.yechan.remittance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "auth.token")
@Validated
public record AuthTokenProperties(
    String salt,
    long accessExpiresIn,
    long refreshExpiresIn
) {
}
