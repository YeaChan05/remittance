package org.yechan.remittance.member;

import org.yechan.remittance.PasswordHashEncoder;

public interface MemberCreateUseCase {

  MemberModel register(MemberProps props);
}


class MemberService implements MemberCreateUseCase {

  private final MemberRepository memberRepository;
  private final PasswordHashEncoder passwordHashEncoder;

  public MemberService(MemberRepository memberRepository, PasswordHashEncoder passwordHashEncoder) {
    this.memberRepository = memberRepository;
    this.passwordHashEncoder = passwordHashEncoder;
  }

  @Override
  public MemberModel register(MemberProps props) {
    // email duplication check
    memberRepository.findByEmail(props.email())
        .ifPresent(model -> {
          throw new MemberException("Email already exists: " + props.email());
        });
    return memberRepository.save(new EncodedMemberProps(props));
  }

  private class EncodedMemberProps implements MemberProps {

    private final MemberProps props;

    public EncodedMemberProps(MemberProps props) {
      this.props = props;
    }

    @Override
    public String name() {
      return props.name();
    }

    @Override
    public String email() {
      return props.email();
    }

    @Override
    public String password() {
      // password hash
      return passwordHashEncoder.encode(props.password());
    }
  }
}
