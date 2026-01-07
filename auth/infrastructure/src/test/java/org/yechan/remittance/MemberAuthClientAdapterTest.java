package org.yechan.remittance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MemberAuthClientAdapterTest {

  @Test
  void verifyDelegatesToMemberInternalApi() {
    var captured = new AtomicReference<LoginVerifyRequest>();
    MemberInternalApi memberInternalApi = request -> {
      captured.set(request);
      return new LoginVerifyResponse(true, 7L);
    };
    var adapter = new MemberAuthClientAdapter(memberInternalApi);

    var result = adapter.verify("user@example.com", "secret");

    assertTrue(result.valid());
    assertEquals(7L, result.memberId());
    assertEquals("user@example.com", captured.get().email());
    assertEquals("secret", captured.get().password());
  }
}
