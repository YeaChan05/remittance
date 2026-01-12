package org.yechan.remittance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransferNotificationProps {

  Long eventId();

  Long transferId();

  Long toAccountId();

  Long fromAccountId();

  BigDecimal amount();

  LocalDateTime occurredAt();
}
