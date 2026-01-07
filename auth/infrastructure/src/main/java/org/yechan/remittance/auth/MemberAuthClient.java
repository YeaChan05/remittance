package org.yechan.remittance.auth;

public interface MemberAuthClient {

  MemberAuthResult verify(String email, String password);
}
