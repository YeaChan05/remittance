package org.yechan.remittance.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.yechan.remittance.EmailGenerator;
import org.yechan.remittance.PasswordGenerator;
import org.yechan.remittance.dto.MemberRegisterRequest;
import org.yechan.remittance.dto.MemberRegisterResponse;

@SpringBootTest
public class PostSpecs {
  
  @Autowired
  RestTestClient restTestClient;

  @Test
  void registerMember() {
    // Arrange
    var name = "test";
    var request = new MemberRegisterRequest(name, EmailGenerator.generate(), PasswordGenerator.generate());

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
    var request = new MemberRegisterRequest("test", email, PasswordGenerator.generate());

    // Act & Assert
    restTestClient.post()
        .uri("/members")
        .body(request)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "pswd1!", "password", "password1", "password!", "12345678!"})
  void registerMemberWithInvalidPassword(
      String password
  ) {
    // Arrange
    var request = new MemberRegisterRequest("test", EmailGenerator.generate(), password);

    // Act & Assert
    restTestClient.post()
        .uri("/members")
        .body(request)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void registerMemberWithDuplicatedEmail() {
    // Arrange
    var email = EmailGenerator.generate();
    var password = PasswordGenerator.generate();

    restTestClient.post()
        .uri("/members")
        .body(new MemberRegisterRequest("test", email, password))
        .exchange()
        .expectStatus().isOk();

    // Act & Assert
    restTestClient.post()
        .uri("/members")
        .body(new MemberRegisterRequest("test", email, password))
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody(String.class)
        .consumeWith(res ->
            assertThat(Objects.requireNonNull(res.getResponseBody()))
                .contains(("Email already exists:")));
  }
}
