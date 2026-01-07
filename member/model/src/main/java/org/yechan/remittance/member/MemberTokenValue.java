package org.yechan.remittance.member;

public record MemberTokenValue(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
