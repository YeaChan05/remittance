package org.yechan.remittance;

public record LoginVerifyResponse(
    boolean valid,
    long memberId
) {

}
