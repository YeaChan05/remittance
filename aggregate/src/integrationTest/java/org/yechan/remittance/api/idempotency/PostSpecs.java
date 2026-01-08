package org.yechan.remittance.api.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.yechan.remittance.EmailGenerator;
import org.yechan.remittance.PasswordGenerator;
import org.yechan.remittance.TokenVerifier;
import org.yechan.remittance.transfer.dto.IdempotencyKeyCreateResponse;
import org.yechan.remittance.member.application.AggregateApplication;
import org.yechan.remittance.member.dto.MemberLoginRequest;
import org.yechan.remittance.member.dto.MemberLoginResponse;
import org.yechan.remittance.member.dto.MemberRegisterRequest;

@SpringBootTest(classes = AggregateApplication.class)
public class PostSpecs {

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  TokenVerifier tokenVerifier;

  @Test
  void issuesIdempotencyKey() {
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
    LocalDateTime before = LocalDateTime.now();

    var authentication = tokenVerifier.verify(loginResponse.accessToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    IdempotencyKeyCreateResponse firstResponse;
    IdempotencyKeyCreateResponse secondResponse;
    try {
      firstResponse = restTestClient.post()
          .uri("/idempotency-keys")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.accessToken())
          .exchange()
          .expectStatus().isOk()
          .expectBody(IdempotencyKeyCreateResponse.class)
          .returnResult()
          .getResponseBody();

      secondResponse = restTestClient.post()
          .uri("/idempotency-keys")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.accessToken())
          .exchange()
          .expectStatus().isOk()
          .expectBody(IdempotencyKeyCreateResponse.class)
          .returnResult()
          .getResponseBody();
    } finally {
      SecurityContextHolder.clearContext();
    }

    assertThat(firstResponse).isNotNull();
    assertThat(secondResponse).isNotNull();
    assertThat(firstResponse.idempotencyKey()).isNotBlank();
    assertThat(secondResponse.idempotencyKey()).isNotBlank();
    assertThat(UUID.fromString(firstResponse.idempotencyKey())).isNotNull();
    assertThat(UUID.fromString(secondResponse.idempotencyKey())).isNotNull();
    assertThat(firstResponse.expiresAt()).isAfter(before);
    assertThat(secondResponse.expiresAt()).isAfter(before);
    assertThat(firstResponse.idempotencyKey()).isNotEqualTo(secondResponse.idempotencyKey());
  }
}
