package org.yechan.remittance.member;

public record LoginVerifyRequest(
    String email,
    String password
) {

}
