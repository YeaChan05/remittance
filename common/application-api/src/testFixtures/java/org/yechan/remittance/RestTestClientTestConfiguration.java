package org.yechan.remittance;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

@AutoConfiguration
public class RestTestClientTestConfiguration {

  @Bean
  RestTestClient restTestClient(WebApplicationContext context) {
    return RestTestClient.bindToApplicationContext(context)
        .build();
  }
}
