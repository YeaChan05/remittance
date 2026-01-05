package org.yechan.remittance.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.yechan.remittance.dto.MemberRegisterRequest;
import org.yechan.remittance.dto.MemberRegisterResponse;

@SpringBootTest
public class PostSpecs {

  private static final String VALID_PASSWORD = "password1!";
  private static final String VALID_EMAIL = "test12@test.com";
  @Autowired
  RestTestClient restTestClient;

  @Test
  void registerMember() {
    // Arrange
    var name = "test";
    var request = new MemberRegisterRequest(name, VALID_EMAIL, VALID_PASSWORD);

    // Act
    var response = restTestClient.post()
        .uri("/members")
        .body(request)
        .exchange()
        .expectStatus().isOk()
        .expectBody(MemberRegisterResponse.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo(name);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "test@", "test@test"})
  void registerMemberWithInvalidEmail(
      String email
  ) {
    // Arrange
    var request = new MemberRegisterRequest("test", email, VALID_PASSWORD);

    // Act & Assert
    restTestClient.post()
        .uri("/members")
        .body(request)
        .exchange()
        .expectStatus().isBadRequest();
  }
}
