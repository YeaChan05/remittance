package org.yechan.remittance.account;

import java.time.LocalDateTime;

record TransferNotificationService(
    AccountRepository accountRepository,
    ProcessedEventRepository processedEventRepository,
    NotificationPushPort notificationPushPort
) implements TransferNotificationUseCase {

  private static final String MESSAGE_TYPE = "TRANSFER_RECEIVED";


  @Override
  public void notify(TransferNotificationProps props) {
    if (processedEventRepository.existsByEventId(props.eventId())) {
      return;
    }

    Long memberId = accountRepository.findById(props::toAccountId)
        .map(AccountModel::memberId)
        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

    TransferNotificationMessage message = new TransferNotificationMessage(
        MESSAGE_TYPE,
        props.transferId(),
        props.amount(),
        props.fromAccountId(),
        props.occurredAt()
    );

    try {
      notificationPushPort.push(memberId, message);
    } catch (RuntimeException ex) {
    }

    processedEventRepository.markProcessed(props.eventId(), LocalDateTime.now());
  }
}
