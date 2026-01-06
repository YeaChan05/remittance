package org.yechan.remittance;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "auth.token")
@Validated
public record AuthTokenProperties(
    @NotBlank String salt,
    @Min(1) long accessExpiresIn,
    @Min(1) long refreshExpiresIn
) {

}
