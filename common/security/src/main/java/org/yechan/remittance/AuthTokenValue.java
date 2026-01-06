package org.yechan.remittance;

public record AuthTokenValue(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
