package org.yechan.remittance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

  @Autowired
  RestTestClient restTestClient;

  @Test
  void test() {
    restTestClient.get().uri("/actuator/health")
        .exchange()
        .expectStatus().isOk();

    restTestClient.get().uri("/actuator/health")
        .exchange()
        .expectStatus().isOk();
  }
}
