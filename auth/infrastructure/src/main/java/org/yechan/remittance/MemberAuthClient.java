package org.yechan.remittance;

public interface MemberAuthClient {

  MemberAuthResult verify(String email, String password);
}
