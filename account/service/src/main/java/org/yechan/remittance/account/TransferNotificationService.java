package org.yechan.remittance.account;

import java.time.LocalDateTime;

class TransferNotificationService implements TransferNotificationUseCase {

  private static final String MESSAGE_TYPE = "TRANSFER_RECEIVED";

  private final AccountRepository accountRepository;
  private final ProcessedEventRepository processedEventRepository;
  private final NotificationPushPort notificationPushPort;

  TransferNotificationService(
      AccountRepository accountRepository,
      ProcessedEventRepository processedEventRepository,
      NotificationPushPort notificationPushPort
  ) {
    this.accountRepository = accountRepository;
    this.processedEventRepository = processedEventRepository;
    this.notificationPushPort = notificationPushPort;
  }

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
