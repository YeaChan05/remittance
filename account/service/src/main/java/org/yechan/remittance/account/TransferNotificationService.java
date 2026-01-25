package org.yechan.remittance.account;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
record TransferNotificationService(
    AccountRepository accountRepository,
    ProcessedEventRepository processedEventRepository,
    NotificationPushPort notificationPushPort
) implements TransferNotificationUseCase {

  private static final String MESSAGE_TYPE = "TRANSFER_RECEIVED";


  @Override
  public void notify(TransferNotificationProps props) {
    log.info("transfer.notification.start eventId={} transferId={}", props.eventId(),
        props.transferId());
    if (processedEventRepository.existsByEventId(props.eventId())) {
      log.info("transfer.notification.duplicate eventId={}", props.eventId());
      return;
    }

    Long memberId = accountRepository.findById(props::toAccountId)
        .map(AccountModel::memberId)
        .orElseThrow(() -> {
          log.warn("transfer.notification.account_not_found toAccountId={}", props.toAccountId());
          return new AccountNotFoundException("Account not found");
        });

    TransferNotificationMessage message = new TransferNotificationMessage(
        MESSAGE_TYPE,
        props.transferId(),
        props.amount(),
        props.fromAccountId(),
        props.occurredAt()
    );

    try {
      log.info("transfer.notification.push memberId={} transferId={}", memberId,
          props.transferId());
      notificationPushPort.push(memberId, message);
    } catch (RuntimeException ex) {
      log.error("transfer.notification.push_failed memberId={} transferId={}", memberId,
          props.transferId(), ex);
    }

    processedEventRepository.markProcessed(props.eventId(), LocalDateTime.now());
     log.info("transfer.notification.processed eventId={} transferId={}", props.eventId(),
         props.transferId());
  }
}
