package org.yechan.remittance.dto;

public record MemberLoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
