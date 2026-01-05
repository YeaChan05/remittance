package org.yechan.remittance;

public interface TokenGenerator {

  MemberTokenValue generate(Long memberId);
}
