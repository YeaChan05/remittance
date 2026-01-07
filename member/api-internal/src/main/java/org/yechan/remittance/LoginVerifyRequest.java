package org.yechan.remittance;

public record LoginVerifyRequest(
    String email,
    String password
) {

}
