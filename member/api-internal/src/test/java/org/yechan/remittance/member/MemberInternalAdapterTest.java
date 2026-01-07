package org.yechan.remittance.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MemberInternalAdapterTest {

  @Test
  void verifyDelegatesToMemberAuthQueryUseCase() {
    var captured = new AtomicReference<MemberLoginProps>();
    MemberAuthQueryUseCase useCase = props -> {
      captured.set(props);
      return new MemberAuthValue(true, 42L);
    };
    var controller = new MemberInternalAdapter(useCase);

    var response = controller.verify(new LoginVerifyRequest("user@example.com", "pass"));

    assertTrue(response.valid());
    assertEquals(42L, response.memberId());
    assertEquals("user@example.com", captured.get().email());
    assertEquals("pass", captured.get().password());
  }
}
