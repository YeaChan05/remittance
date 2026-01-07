package org.yechan.remittance;

public interface AuthLoginUseCase {

  AuthTokenValue login(AuthLoginProps props);
}


class AuthService implements AuthLoginUseCase {

  private final MemberAuthClient memberAuthClient;
  private final TokenGenerator tokenGenerator;

  public AuthService(MemberAuthClient memberAuthClient, TokenGenerator tokenGenerator) {
    this.memberAuthClient = memberAuthClient;
    this.tokenGenerator = tokenGenerator;
  }

  @Override
  public AuthTokenValue login(AuthLoginProps props) {
    var result = memberAuthClient.verify(props.email(), props.password());
    if (!result.valid()) {
      throw new AuthInvalidCredentialException("Invalid credentials");
    }
    return tokenGenerator.generate(result.memberId());
  }
}
