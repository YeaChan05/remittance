package org.yechan.remittance.member;

public record LoginVerifyResponse(
    boolean valid,
    long memberId
) {

}
