package org.yechan.remittance.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;

class TransferSnapshotUtilTest {

  @Test
  void roundTripsSnapshot() {
    TransferSnapshotUtil util = new TransferSnapshotUtil(new ObjectMapper());
    TransferResult result = new TransferResult(TransferStatusValue.SUCCEEDED, 10L, null);

    String snapshot = util.toSnapshot(result);
    TransferResult restored = util.fromSnapshot(snapshot);

    assertEquals(result.status(), restored.status());
    assertEquals(result.transferId(), restored.transferId());
    assertEquals(result.errorCode(), restored.errorCode());
  }

  @Test
  void rejectsEmptySnapshot() {
    TransferSnapshotUtil util = new TransferSnapshotUtil(new ObjectMapper());

    assertThrows(IllegalArgumentException.class, () -> util.fromSnapshot(null));
    assertThrows(IllegalArgumentException.class, () -> util.fromSnapshot(""));
    assertThrows(IllegalArgumentException.class, () -> util.fromSnapshot("   "));
  }

  @Test
  void failsOnInvalidJson() {
    TransferSnapshotUtil util = new TransferSnapshotUtil(new ObjectMapper());

    assertThrows(TransferException.class, () -> util.fromSnapshot("not-json"));
  }

  @Test
  void failsWhenObjectMapperCannotSerialize() {
    ObjectMapper failingMapper = new ObjectMapper() {
      @Override
      public String writeValueAsString(Object value) throws JsonProcessingException {
        throw new JsonProcessingException("fail") {
        };
      }
    };
    TransferSnapshotUtil util = new TransferSnapshotUtil(failingMapper);

    assertThrows(TransferException.class,
        () -> util.toHashRequest(new TestRequestProps()));
  }

  @Test
  void hashesRequestDeterministically() {
    TransferSnapshotUtil util = new TransferSnapshotUtil(new ObjectMapper());
    TransferRequestProps props = new TestRequestProps();

    String hash1 = util.toHashRequest(props);
    String hash2 = util.toHashRequest(props);

    assertEquals(hash1, hash2);
    assertTrue(hash1.length() > 0);
  }

  private static class TestRequestProps implements TransferRequestProps {

    @Override
    public Long fromAccountId() {
      return 1L;
    }

    @Override
    public Long toAccountId() {
      return 2L;
    }

    @Override
    public BigDecimal amount() {
      return new BigDecimal("100.00");
    }

    @Override
    public TransferScopeValue scope() {
      return TransferScopeValue.TRANSFER;
    }

    @Override
    public BigDecimal fee() {
      return BigDecimal.ONE;
    }
  }
}
