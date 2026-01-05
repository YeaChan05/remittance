package org.yechan.remittance;

public record Member(Long memberId,
                     String name,
                     String email,
                     String password) implements MemberModel {

}
