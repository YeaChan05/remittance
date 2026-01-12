package org.yechan.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yechan.remittance.AggregateApplication;
import org.yechan.remittance.TestContainerSetup;

@SpringBootTest(classes = AggregateApplication.class)
class AggregateApplicationTest extends TestContainerSetup {

  @Test
  void contextLoads() {
    AggregateApplication.main(new String[]{});
  }
}
