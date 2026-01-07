package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.member.LoginVerifyRequest;
import org.yechan.remittance.member.LoginVerifyResponse;
import org.yechan.remittance.member.MemberInternalApi;

class MemberAuthClientAdapterIntegrationTest {

  @Test
  void verifyDelegatesToMemberInternalApi() {
    var captured = new AtomicReference<LoginVerifyRequest>();
    MemberInternalApi memberInternalApi = request -> {
      captured.set(request);
      return new LoginVerifyResponse(true, 7L);
    };
    var adapter = new MemberAuthClientAdapter(memberInternalApi);

    var result = adapter.verify("user@example.com", "secret");

    assertThat(result.valid()).isTrue();
    assertThat(result.memberId()).isEqualTo(7L);
    assertThat(captured.get().email()).isEqualTo("user@example.com");
    assertThat(captured.get().password()).isEqualTo("secret");
  }
}
