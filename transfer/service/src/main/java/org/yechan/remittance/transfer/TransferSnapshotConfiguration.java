package org.yechan.remittance.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TransferSnapshotConfiguration {

  @Bean
  @ConditionalOnMissingBean
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  TransferSnapshotUtil transferSnapshotUtil(ObjectMapper objectMapper) {
    return new TransferSnapshotUtil(objectMapper);
  }
}
