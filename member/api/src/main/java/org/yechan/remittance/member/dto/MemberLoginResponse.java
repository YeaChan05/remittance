package org.yechan.remittance.member.dto;

public record MemberLoginResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {

}
