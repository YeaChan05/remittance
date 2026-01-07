package org.yechan.remittance.member;

import static org.yechan.remittance.Status.AUTHENTICATION_FAILED;

import org.yechan.remittance.PasswordHashEncoder;
import org.yechan.remittance.TokenGenerator;

public interface MemberQueryUseCase {

  MemberTokenValue login(MemberLoginProps props);
}


class MemberQueryService implements MemberQueryUseCase {

  private final MemberRepository memberRepository;
  private final PasswordHashEncoder passwordHashEncoder;
  private final TokenGenerator tokenGenerator;

  public MemberQueryService(MemberRepository memberRepository,
      PasswordHashEncoder passwordHashEncoder,
      TokenGenerator tokenGenerator) {
    this.memberRepository = memberRepository;
    this.passwordHashEncoder = passwordHashEncoder;
    this.tokenGenerator = tokenGenerator;
  }

  @Override
  public MemberTokenValue login(MemberLoginProps props) {
    var member = memberRepository.findByEmail(props.email())
        .orElseThrow(() -> new MemberException("Member not found"));

    if (!passwordHashEncoder.matches(props.password(), member.password())) {
      throw new MemberPermissionDeniedException(AUTHENTICATION_FAILED, "Invalid credentials");
    }

    var token = tokenGenerator.generate(member.memberId());
    return new MemberTokenValue(token.accessToken(), token.refreshToken(), token.expiresIn());
  }
}
