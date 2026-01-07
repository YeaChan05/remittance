package org.yechan.remittance.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.yechan.remittance.member.MemberProps;
import org.yechan.remittance.member.member.repository.TestApplication;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MemberRepositoryAutoConfiguration.class)
@ContextConfiguration(classes = TestApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MemberJpaRepositoryTest {

  @Autowired
  MemberJpaRepository memberRepository;
  @Autowired
  EntityManager entityManager;

  @Test
  void registerMember() {
    MemberEntity member = MemberEntity.create(
        new MemberProps() {
          @Override
          public String name() {
            return "name";
          }

          @Override
          public String email() {
            return "test@test.com";
          }

          @Override
          public String password() {
            return "qweasdqwe";
          }
        }
    );
    var saved = memberRepository.save(member);
    entityManager.flush();

    assertThat(saved.memberId()).isNotNull();
    var byId = memberRepository.findById(saved.memberId());
    assertThat(byId).isPresent();
    assertThat(byId.get()).isEqualTo(saved);
  }
}
