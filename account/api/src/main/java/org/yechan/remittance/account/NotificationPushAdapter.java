package org.yechan.remittance.account;

record NotificationPushAdapter(
    NotificationSessionRegistry registry
) implements NotificationPushPort {

  @Override
  public boolean push(Long memberId, TransferNotificationMessage message) {
    return registry.push(memberId, message);
  }
}
