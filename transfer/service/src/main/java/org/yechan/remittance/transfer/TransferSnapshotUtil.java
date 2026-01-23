package org.yechan.remittance.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

public class TransferSnapshotUtil {

  private final ObjectMapper objectMapper;

  public TransferSnapshotUtil(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String toSnapshot(TransferResult result) {
    return writeJson(new SnapshotPayload(
        result.status().name(),
        result.transferId(),
        result.errorCode()
    ));
  }

  public TransferResult fromSnapshot(String snapshot) {
    if (snapshot == null || snapshot.isBlank()) {
      throw new IllegalArgumentException("Snapshot is empty");
    }
    SnapshotPayload payload = readJson(snapshot, SnapshotPayload.class);
    TransferStatusValue status = TransferStatusValue.valueOf(payload.status());
    return new TransferResult(status, payload.transferId(), payload.errorCode());
  }

  public String toHashRequest(TransferRequestProps props) {
    String canonical = writeJson(new HashPayload(
        props.fromAccountId(),
        props.toAccountId(),
        props.amount()
    ));
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
      return toHex(hashed);
    } catch (NoSuchAlgorithmException ex) {
      throw new TransferException("Hash algorithm not found");
    }
  }

  public String toOutboxPayload(
      TransferModel transfer,
      TransferRequestProps props,
      LocalDateTime now
  ) {
    return writeJson(new OutboxPayload(
        transfer.transferId(),
        props.fromAccountId(),
        props.toAccountId(),
        props.amount(),
        now.toString()
    ));
  }

  private String writeJson(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException ex) {
      throw new TransferException("Json serialization failed");
    }
  }

  private <T> T readJson(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JsonProcessingException ex) {
      throw new TransferException("Json deserialization failed");
    }
  }

  private String toHex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte value : bytes) {
      builder.append(String.format("%02x", value));
    }
    return builder.toString();
  }

  private record SnapshotPayload(String status, Long transferId, String errorCode) {

  }

  private record HashPayload(Long fromAccountId, Long toAccountId, BigDecimal amount) {

  }

  private record OutboxPayload(
      Long transferId,
      Long fromAccountId,
      Long toAccountId,
      BigDecimal amount,
      String completedAt
  ) {

  }
}
