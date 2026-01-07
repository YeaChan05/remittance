package org.yechan.remittance.auth;

import org.yechan.remittance.member.LoginVerifyRequest;
import org.yechan.remittance.member.MemberInternalApi;

public class MemberAuthClientAdapter implements MemberAuthClient {

  private final MemberInternalApi memberInternalApi;

  public MemberAuthClientAdapter(MemberInternalApi memberInternalApi) {
    this.memberInternalApi = memberInternalApi;
  }

  @Override
  public MemberAuthResult verify(String email, String password) {
    var response = memberInternalApi.verify(new LoginVerifyRequest(email, password));
    return new MemberAuthResult(response.valid(), response.memberId());
  }
}
