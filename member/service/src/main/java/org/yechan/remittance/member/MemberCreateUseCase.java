package org.yechan.remittance.member;

import lombok.extern.slf4j.Slf4j;
import org.yechan.remittance.PasswordHashEncoder;

public interface MemberCreateUseCase {

  MemberModel register(MemberProps props);
}

@Slf4j
record MemberService(
    MemberRepository memberRepository,
    PasswordHashEncoder passwordHashEncoder
) implements MemberCreateUseCase {

  @Override
  public MemberModel register(MemberProps props) {
     log.info("member.register.start");
    // email duplication check
    memberRepository.findByEmail(props.email())
        .ifPresent(model -> {
           log.warn("member.register.duplicate_email email={}", props.email());
          throw new MemberException("Email already exists: " + props.email());
        });
     log.info("member.register.persist");
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
      try {
         log.debug("member.register.password_hashing");
        return passwordHashEncoder.encode(props.password());
      } catch (IllegalArgumentException e) {
         log.warn("member.register.invalid_password");
        throw new MemberException("Invalid password: " + props.password() + ", " + e.getMessage());
      }
    }
  }
}
