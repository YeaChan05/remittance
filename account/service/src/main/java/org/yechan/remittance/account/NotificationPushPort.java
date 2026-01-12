package org.yechan.remittance.account;

public interface NotificationPushPort {

  boolean push(Long memberId, TransferNotificationMessage message);
}
