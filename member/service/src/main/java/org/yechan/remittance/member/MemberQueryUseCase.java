package org.yechan.remittance.member;

import static org.yechan.remittance.Status.AUTHENTICATION_FAILED;

import lombok.extern.slf4j.Slf4j;
import org.yechan.remittance.PasswordHashEncoder;
import org.yechan.remittance.TokenGenerator;

public interface MemberQueryUseCase {

  MemberTokenValue login(MemberLoginProps props);
}

@Slf4j
record MemberQueryService(
    MemberRepository memberRepository,
    PasswordHashEncoder passwordHashEncoder,
    TokenGenerator tokenGenerator
) implements MemberQueryUseCase {

  @Override
  public MemberTokenValue login(MemberLoginProps props) {
     log.info("member.login.start");
    var member = memberRepository.findByEmail(props.email())
        .orElseThrow(() -> {
           log.warn("member.login.not_found");
          return new MemberException("Member not found");
        });

    if (!passwordHashEncoder.matches(props.password(), member.password())) {
       log.warn("member.login.invalid_credentials memberId={}", member.memberId());
      throw new MemberPermissionDeniedException(AUTHENTICATION_FAILED, "Invalid credentials");
    }

    var token = tokenGenerator.generate(member.memberId());
     log.info("member.login.success memberId={}", member.memberId());
    return new MemberTokenValue(token.accessToken(), token.refreshToken(), token.expiresIn());
  }
}
