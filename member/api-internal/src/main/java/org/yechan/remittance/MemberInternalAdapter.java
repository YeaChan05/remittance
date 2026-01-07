package org.yechan.remittance;

class MemberInternalAdapter implements MemberInternalApi {

  private final MemberAuthQueryUseCase memberAuthQueryUseCase;

  MemberInternalAdapter(MemberAuthQueryUseCase memberAuthQueryUseCase) {
    this.memberAuthQueryUseCase = memberAuthQueryUseCase;
  }

  @Override
  public LoginVerifyResponse verify(LoginVerifyRequest request) {
    var result = memberAuthQueryUseCase.verify(new MemberLoginRequest(request.email(), request.password()));
    return new LoginVerifyResponse(result.valid(), result.memberId());
  }

  private record MemberLoginRequest(String email, String password) implements MemberLoginProps {

  }
}
