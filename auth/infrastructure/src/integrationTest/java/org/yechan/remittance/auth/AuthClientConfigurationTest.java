package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.yechan.remittance.member.LoginVerifyResponse;
import org.yechan.remittance.member.MemberInternalApi;

class AuthClientConfigurationTest {

  @Test
  void providesMemberAuthClientAdapter() {
    MemberInternalApi memberInternalApi = request -> new LoginVerifyResponse(true, 3L);
    var configuration = new AuthClientConfiguration();

    var client = configuration.memberAuthClient(memberInternalApi);

    assertThat(client).isInstanceOf(MemberAuthClientAdapter.class);
    assertThat(client.verify("user@example.com", "password").memberId()).isEqualTo(3L);
  }
}
