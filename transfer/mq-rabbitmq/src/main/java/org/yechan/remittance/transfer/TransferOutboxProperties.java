package org.yechan.remittance.transfer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("transfer.outbox")
class TransferOutboxProperties {

  private int batchSize = 100;
  private long publishDelayMs = 1000;

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public long getPublishDelayMs() {
    return publishDelayMs;
  }

  public void setPublishDelayMs(long publishDelayMs) {
    this.publishDelayMs = publishDelayMs;
  }
}
