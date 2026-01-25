package org.yechan.remittance.member;

import lombok.extern.slf4j.Slf4j;
import org.yechan.remittance.PasswordHashEncoder;

public interface MemberAuthQueryUseCase {

  MemberAuthValue verify(MemberLoginProps props);
}

@Slf4j
record MemberAuthQueryService(
    MemberRepository memberRepository,
    PasswordHashEncoder passwordHashEncoder
) implements MemberAuthQueryUseCase {

  @Override
  public MemberAuthValue verify(MemberLoginProps props) {
     log.info("member.auth.verify.start");
    var member = memberRepository.findByEmail(props.email());
    if (member.isEmpty()) {
       log.warn("member.auth.verify.not_found");
      return new MemberAuthValue(false, 0L);
    }
    if (!passwordHashEncoder.matches(props.password(), member.get().password())) {
       log.warn("member.auth.verify.password_mismatch memberId={}", member.get().memberId());
      return new MemberAuthValue(false, 0L);
    }
     log.info("member.auth.verify.success memberId={}", member.get().memberId());
    return new MemberAuthValue(true, member.get().memberId());
  }
}
