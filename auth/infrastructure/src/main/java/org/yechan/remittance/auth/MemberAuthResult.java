package org.yechan.remittance.auth;

public record MemberAuthResult(
    boolean valid,
    long memberId
) {

}
