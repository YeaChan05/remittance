package org.yechan.remittance.transfer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("transfer.outbox.publisher")
class TransferEventPublisherProperties {

  private String exchange = "transfer.exchange";
  private String routingKey = "transfer.completed";

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }
}
