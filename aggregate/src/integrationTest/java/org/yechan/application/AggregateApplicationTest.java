package org.yechan.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yechan.remittance.application.AggregateApplication;

@SpringBootTest(classes = AggregateApplication.class)
class AggregateApplicationTest {

  @Test
  void contextLoads() {
    AggregateApplication.main(new String[] {});
  }
}
