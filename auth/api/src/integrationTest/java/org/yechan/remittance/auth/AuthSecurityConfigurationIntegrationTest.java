package org.yechan.remittance.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthSecurityConfigurationIntegrationTest {

  @Test
  void providesAuthorizeHttpRequestsCustomizer() {
    var configuration = new AuthSecurityConfiguration();

    var customizer = configuration.authorizeHttpRequestsCustomizer();

    assertThat(customizer).isNotNull();
  }
}
