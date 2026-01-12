package org.yechan.remittance.api.account;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.yechan.remittance.TestContainerSetup;
import org.yechan.remittance.EmailGenerator;
import org.yechan.remittance.PasswordGenerator;
import org.yechan.remittance.TokenVerifier;
import org.yechan.remittance.account.dto.AccountCreateRequest;
import org.yechan.remittance.account.dto.AccountCreateResponse;
import org.yechan.remittance.AggregateApplication;
import org.yechan.remittance.member.dto.MemberLoginRequest;
import org.yechan.remittance.member.dto.MemberLoginResponse;
import org.yechan.remittance.member.dto.MemberRegisterRequest;

@SpringBootTest(classes = AggregateApplication.class)
public class PostSpecs extends TestContainerSetup {

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  TokenVerifier tokenVerifier;

  @Test
  void createAccount() {
    String token = login();
    var authentication = tokenVerifier.verify(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    try {
      var request = new AccountCreateRequest("090", "123-456", "생활비");

      var response = restTestClient.post()
          .uri("/accounts")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
          .body(request)
          .exchange()
          .expectStatus().isOk()
          .expectBody(AccountCreateResponse.class)
          .returnResult()
          .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.accountId()).isNotNull();
      assertThat(response.accountName()).isEqualTo("생활비");
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private String login() {
    var email = EmailGenerator.generate();
    var password = PasswordGenerator.generate();

    restTestClient.post()
        .uri("/members")
        .body(new MemberRegisterRequest("test", email, password))
        .exchange()
        .expectStatus().isOk();

    var loginResponse = restTestClient.post()
        .uri("/login")
        .body(new MemberLoginRequest(email, password))
        .exchange()
        .expectStatus().isOk()
        .expectBody(MemberLoginResponse.class)
        .returnResult()
        .getResponseBody();

    assertThat(loginResponse).isNotNull();
    assertThat(loginResponse.accessToken()).isNotBlank();
    return loginResponse.accessToken();
  }
}
