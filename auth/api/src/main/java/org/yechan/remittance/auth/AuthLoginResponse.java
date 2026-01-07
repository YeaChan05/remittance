package org.yechan.remittance.auth;

public record AuthLoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
