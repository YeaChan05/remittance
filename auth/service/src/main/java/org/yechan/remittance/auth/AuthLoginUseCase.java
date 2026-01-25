package org.yechan.remittance.auth;

import lombok.extern.slf4j.Slf4j;
import org.yechan.remittance.AuthTokenValue;
import org.yechan.remittance.TokenGenerator;

public interface AuthLoginUseCase {

  AuthTokenValue login(AuthLoginProps props);
}

@Slf4j
record AuthService(
    MemberAuthClient memberAuthClient,
    TokenGenerator tokenGenerator
) implements AuthLoginUseCase {


  @Override
  public AuthTokenValue login(AuthLoginProps props) {
     log.info("auth.login.start");
    var result = memberAuthClient.verify(props.email(), props.password());
    if (!result.valid()) {
       log.warn("auth.login.invalid_credentials");
      throw new AuthInvalidCredentialException("Invalid credentials");
    }
     log.info("auth.login.success memberId={}", result.memberId());
    return tokenGenerator.generate(result.memberId());
  }
}
