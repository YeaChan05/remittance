package org.yechan.remittance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TransferNotificationPayloadParser {

  private static final Pattern FIELD_PATTERN = Pattern.compile(
      "\"(\\w+)\"\\s*:\\s*(\"[^\"]*\"|[-]?[0-9]+(?:\\.[0-9]+)?)");

  TransferNotificationProps parse(Long eventId, String payload) {
    Map<String, String> values = parsePayload(payload);
    return new TransferNotificationMessage(
        eventId,
        Long.parseLong(values.get("transferId")),
        Long.parseLong(values.get("toAccountId")),
        Long.parseLong(values.get("fromAccountId")),
        new BigDecimal(values.get("amount")),
        LocalDateTime.parse(values.get("completedAt"))
    );
  }

  private Map<String, String> parsePayload(String payload) {
    Map<String, String> values = new HashMap<>();
    Matcher matcher = FIELD_PATTERN.matcher(payload);
    while (matcher.find()) {
      String key = matcher.group(1);
      String rawValue = matcher.group(2);
      String value = rawValue.startsWith("\"")
          ? rawValue.substring(1, rawValue.length() - 1)
          : rawValue;
      values.put(key, value);
    }
    return values;
  }

  private record TransferNotificationMessage(
      Long eventId,
      Long transferId,
      Long toAccountId,
      Long fromAccountId,
      BigDecimal amount,
      LocalDateTime occurredAt
  ) implements TransferNotificationProps {

  }
}
