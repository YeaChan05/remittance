package org.yechan.remittance.member;

public record Member(Long memberId,
                     String name,
                     String email,
                     String password) implements MemberModel {

}
