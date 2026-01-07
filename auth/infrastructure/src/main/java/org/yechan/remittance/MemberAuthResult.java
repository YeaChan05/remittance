package org.yechan.remittance;

public record MemberAuthResult(
    boolean valid,
    long memberId
) {

}
