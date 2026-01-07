package org.yechan.remittance.member;

public record MemberAuthValue(
    boolean valid,
    long memberId
) {

}
