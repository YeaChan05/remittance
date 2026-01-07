package org.yechan.remittance.member;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.yechan.remittance.TokenGenerator;

@SpringBootTest(classes = {
    CommonSecurityAutoConfigurationTest.TestApplication.class,
    CommonSecurityAutoConfigurationTest.RestTestClientConfiguration.class
})
@TestPropertySource(properties = {
    "auth.token.salt=test-salt",
    "auth.token.access-expires-in=3600",
    "auth.token.refresh-expires-in=7200"
})
class CommonSecurityAutoConfigurationTest {

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  TokenGenerator tokenGenerator;

  @Test
  void returnsUnauthorizedWhenTokenIsMissing() throws Exception {
    restTestClient.get()
        .uri("/secure")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void returnsUnauthorizedWhenTokenIsInvalid() throws Exception {
    restTestClient.get()
        .uri("/secure")
        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void allowsRequestWhenTokenIsValid() throws Exception {
    var token = tokenGenerator.generate(1L).accessToken();

    restTestClient.get()
        .uri("/secure")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .isEqualTo("1");
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {

    @RestController
    static class TestController {

      @GetMapping("/secure")
      String secure(Authentication authentication) {
        return authentication.getName();
      }
    }
  }

  @TestConfiguration
  static class RestTestClientConfiguration {

    @Bean
    RestTestClient restTestClient(WebApplicationContext context) {
      var mockMvc = MockMvcBuilders.webAppContextSetup(context)
          .apply(springSecurity())
          .build();
      return RestTestClient.bindTo(mockMvc).build();
    }
  }
}
