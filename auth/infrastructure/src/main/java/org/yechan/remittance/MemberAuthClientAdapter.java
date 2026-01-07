package org.yechan.remittance;

class MemberAuthClientAdapter implements MemberAuthClient {

  private final MemberInternalApi memberInternalApi;

  MemberAuthClientAdapter(MemberInternalApi memberInternalApi) {
    this.memberInternalApi = memberInternalApi;
  }

  @Override
  public MemberAuthResult verify(String email, String password) {
    var response = memberInternalApi.verify(new LoginVerifyRequest(email, password));
    return new MemberAuthResult(response.valid(), response.memberId());
  }
}
