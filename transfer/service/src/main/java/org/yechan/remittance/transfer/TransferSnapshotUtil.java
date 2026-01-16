package org.yechan.remittance.transfer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

public class TransferSnapshotUtil {

  public static String toSnapshot(TransferResult result) {
    return "transferId=" + safeValue(result.transferId())
        + "|status=" + result.status().name()
        + "|errorCode=" + safeValue(result.errorCode());
  }

  private static String safeValue(Object value) {
    return Objects.toString(value, "");
  }

  public static TransferResult fromSnapshot(String snapshot) {
    if (snapshot == null || snapshot.isBlank()) {
      throw new IllegalArgumentException("Snapshot is empty");
    }
    Map<String, String> values = parseSnapshot(snapshot);

    TransferStatusValue status = TransferStatusValue.valueOf(values.get("status"));
    Long transferId = parseLong(values.get("transferId"));
    String errorCode = values.get("errorCode");
    if (errorCode != null && errorCode.isBlank()) {
      errorCode = null;
    }
    return new TransferResult(status, transferId, errorCode);
  }

  private static @NonNull Map<String, String> parseSnapshot(String snapshot) {
    return Arrays.stream(snapshot.split("\\|"))
        .map(token -> token.split("=", 2))
        .filter(pair -> pair.length == 2)
        .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1], (a, b) -> b));
  }

  private static Long parseLong(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Long.parseLong(value);
  }

  public static String toHashRequest(TransferRequestProps props) {
    String canonical = "fromAccountId=" + props.fromAccountId()
        + "|toAccountId=" + props.toAccountId()
        + "|amount=" + props.amount();
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
      return toHex(hashed);
    } catch (NoSuchAlgorithmException ex) {
      throw new TransferException("Hash algorithm not found");
    }
  }

  public static String toOutboxPayload(
      TransferModel transfer,
      TransferRequestProps props,
      LocalDateTime now
  ) {
    return "transferId=" + transfer.transferId()
        + "|fromAccountId=" + props.fromAccountId()
        + "|toAccountId=" + props.toAccountId()
        + "|amount=" + props.amount()
        + "|completedAt=" + now;
  }

  private static String toHex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }
}
