package org.yechan.remittance;

public record MemberAuthValue(
    boolean valid,
    long memberId
) {

}
