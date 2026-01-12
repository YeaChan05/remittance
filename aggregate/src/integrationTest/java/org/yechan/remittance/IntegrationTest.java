package org.yechan.remittance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(classes = AggregateApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

  @Autowired
  RestTestClient restTestClient;

  @org.junit.jupiter.api.Test
  void test() {
    restTestClient.get().uri("/actuator/health")
        .exchange()
        .expectStatus().isOk();

    restTestClient.get().uri("/actuator/info")
        .exchange()
        .expectStatus().isOk();
  }
}
