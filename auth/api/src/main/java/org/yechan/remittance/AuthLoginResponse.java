package org.yechan.remittance;

public record AuthLoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
