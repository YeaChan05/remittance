package org.yechan.remittance.member;

import org.yechan.remittance.PasswordHashEncoder;

public interface MemberAuthQueryUseCase {

  MemberAuthValue verify(MemberLoginProps props);
}


class MemberAuthQueryService implements MemberAuthQueryUseCase {

  private final MemberRepository memberRepository;
  private final PasswordHashEncoder passwordHashEncoder;

  public MemberAuthQueryService(MemberRepository memberRepository,
      PasswordHashEncoder passwordHashEncoder) {
    this.memberRepository = memberRepository;
    this.passwordHashEncoder = passwordHashEncoder;
  }

  @Override
  public MemberAuthValue verify(MemberLoginProps props) {
    var member = memberRepository.findByEmail(props.email());
    if (member.isEmpty()) {
      return new MemberAuthValue(false, 0L);
    }
    if (!passwordHashEncoder.matches(props.password(), member.get().password())) {
      return new MemberAuthValue(false, 0L);
    }
    return new MemberAuthValue(true, member.get().memberId());
  }
}
