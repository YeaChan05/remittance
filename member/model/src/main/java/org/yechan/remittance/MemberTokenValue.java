package org.yechan.remittance;

public record MemberTokenValue(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
