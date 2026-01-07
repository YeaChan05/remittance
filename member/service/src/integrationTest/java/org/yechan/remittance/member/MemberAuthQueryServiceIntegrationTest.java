package org.yechan.remittance.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.PasswordHashEncoder;

class MemberAuthQueryServiceIntegrationTest {

  @Test
  void returnsInvalidWhenMemberDoesNotExist() {
    MemberRepository memberRepository = new MemberRepository() {
      @Override
      public MemberModel save(MemberProps props) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Optional<MemberModel> findById(MemberIdentifier identifier) {
        return Optional.empty();
      }

      @Override
      public Optional<MemberModel> findByEmail(String email) {
        return Optional.empty();
      }
    };
    PasswordHashEncoder passwordHashEncoder = new PasswordHashEncoder() {
      @Override
      public String encode(String password) {
        return "encoded";
      }

      @Override
      public boolean matches(String password, String encodedPassword) {
        return true;
      }
    };
    MemberAuthQueryUseCase useCase = new MemberAuthQueryService(memberRepository,
        passwordHashEncoder);

    MemberAuthValue result = useCase.verify(new TestLoginProps());

    assertThat(result.valid()).isFalse();
    assertThat(result.memberId()).isEqualTo(0L);
  }

  @Test
  void returnsInvalidWhenPasswordMismatch() {
    MemberRepository memberRepository = new MemberRepository() {
      @Override
      public MemberModel save(MemberProps props) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Optional<MemberModel> findById(MemberIdentifier identifier) {
        return Optional.empty();
      }

      @Override
      public Optional<MemberModel> findByEmail(String email) {
        return Optional.of(new TestMember(7L, "hashed"));
      }
    };
    PasswordHashEncoder passwordHashEncoder = new PasswordHashEncoder() {
      @Override
      public String encode(String password) {
        return "encoded";
      }

      @Override
      public boolean matches(String password, String encodedPassword) {
        return false;
      }
    };
    MemberAuthQueryUseCase useCase = new MemberAuthQueryService(memberRepository,
        passwordHashEncoder);

    MemberAuthValue result = useCase.verify(new TestLoginProps());

    assertThat(result.valid()).isFalse();
    assertThat(result.memberId()).isEqualTo(0L);
  }

  @Test
  void returnsValidWhenMemberExistsAndPasswordMatches() {
    MemberRepository memberRepository = new MemberRepository() {
      @Override
      public MemberModel save(MemberProps props) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Optional<MemberModel> findById(MemberIdentifier identifier) {
        return Optional.empty();
      }

      @Override
      public Optional<MemberModel> findByEmail(String email) {
        return Optional.of(new TestMember(9L, "hashed"));
      }
    };
    PasswordHashEncoder passwordHashEncoder = new PasswordHashEncoder() {
      @Override
      public String encode(String password) {
        return "encoded";
      }

      @Override
      public boolean matches(String password, String encodedPassword) {
        return true;
      }
    };
    MemberAuthQueryUseCase useCase = new MemberAuthQueryService(memberRepository,
        passwordHashEncoder);

    MemberAuthValue result = useCase.verify(new TestLoginProps());

    assertThat(result.valid()).isTrue();
    assertThat(result.memberId()).isEqualTo(9L);
  }

  private static class TestLoginProps implements MemberLoginProps {

    @Override
    public String email() {
      return "user@example.com";
    }

    @Override
    public String password() {
      return "password!1";
    }
  }

  private record TestMember(Long memberId, String password) implements MemberModel {

    @Override
    public String name() {
      return "name";
    }

    @Override
    public String email() {
      return "user@example.com";
    }
  }
}
