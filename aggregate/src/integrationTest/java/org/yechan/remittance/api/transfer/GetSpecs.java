package org.yechan.remittance.api.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.support.TransactionTemplate;
import org.yechan.remittance.AggregateApplication;
import org.yechan.remittance.TestContainerSetup;
import org.yechan.remittance.TransferTestFixtures;
import org.yechan.remittance.TransferTestFixturesConfig;
import org.yechan.remittance.transfer.dto.IdempotencyKeyCreateResponse;
import org.yechan.remittance.transfer.dto.TransferQueryResponse;
import org.yechan.remittance.transfer.dto.TransferQueryResponse.TransferItem;
import org.yechan.remittance.transfer.dto.TransferRequest;

@SpringBootTest(classes = AggregateApplication.class)
@Import(TransferTestFixturesConfig.class)
class GetSpecs extends TestContainerSetup {

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  TransferTestFixtures fixtures;

  @Autowired
  EntityManager entityManager;

  @Autowired
  TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnHistorySortedByCompletedAtDesc() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금",
        BigDecimal.valueOf(5_000_000L));
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", BigDecimal.ZERO);

    var firstKey = issueIdempotencyKey(result.auth().accessToken());
    var firstResponse = transfer(result.auth().accessToken(), firstKey,
        fromAccount.accountId(), toAccount.accountId(), BigDecimal.valueOf(100_000L));

    var secondKey = issueIdempotencyKey(result.auth().accessToken());
    var secondResponse = transfer(result.auth().accessToken(), secondKey,
        fromAccount.accountId(), toAccount.accountId(), BigDecimal.valueOf(200_000L));

    LocalDateTime older = LocalDateTime.now().minusHours(2);
    LocalDateTime newer = LocalDateTime.now().minusHours(1);
    updateCompletedAt(firstResponse.transferId(), older);
    updateCompletedAt(secondResponse.transferId(), newer);

    var response = query(result.auth().accessToken(), fromAccount.accountId(), null, null, null);

    assertThat(response.transfers()).hasSize(2);
    assertThat(response.transfers().getFirst().transferId()).isEqualTo(secondResponse.transferId());
    assertThat(response.transfers().get(1).transferId()).isEqualTo(firstResponse.transferId());
    assertThat(response.transfers()).isSortedAccordingTo(
        Comparator.comparing(TransferItem::completedAt).reversed());
  }

  @Test
  void shouldIncludeReceivedHistory() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금",
        BigDecimal.valueOf(1_000_000L));
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", BigDecimal.ZERO);

    var key = issueIdempotencyKey(result.auth().accessToken());
    var response = transfer(result.auth().accessToken(), key,
        fromAccount.accountId(), toAccount.accountId(), BigDecimal.valueOf(100_000L));

    var queryResponse = query(result.auth().accessToken(), toAccount.accountId(), null, null, null);

    assertThat(queryResponse.transfers()).hasSize(1);
    assertThat(queryResponse.transfers().getFirst().transferId()).isEqualTo(response.transferId());
    assertThat(queryResponse.transfers().getFirst().fromAccountId()).isEqualTo(
        fromAccount.accountId());
    assertThat(queryResponse.transfers().getFirst().toAccountId()).isEqualTo(toAccount.accountId());
  }

  @Test
  void shouldApplyLimit() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금",
        BigDecimal.valueOf(2_000_000L));
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", BigDecimal.ZERO);

    var firstKey = issueIdempotencyKey(result.auth().accessToken());
    transfer(result.auth().accessToken(), firstKey,
        fromAccount.accountId(), toAccount.accountId(), BigDecimal.valueOf(100_000L));

    var secondKey = issueIdempotencyKey(result.auth().accessToken());
    transfer(result.auth().accessToken(), secondKey,
        fromAccount.accountId(), toAccount.accountId(), BigDecimal.valueOf(200_000L));

    var response = query(result.auth().accessToken(), fromAccount.accountId(), null, null, 1);

    assertThat(response.transfers()).hasSize(1);
  }

  private TransferQueryResponse query(
      String accessToken,
      Long accountId,
      LocalDateTime from,
      LocalDateTime to,
      Integer limit
  ) {
    var response = restTestClient.get()
        .uri(uriBuilder -> uriBuilder.path("/transfers")
            .queryParam("accountId", accountId)
            .queryParamIfPresent("from", java.util.Optional.ofNullable(from))
            .queryParamIfPresent("to", java.util.Optional.ofNullable(to))
            .queryParamIfPresent("limit", java.util.Optional.ofNullable(limit))
            .build())
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .exchange()
        .expectStatus().isOk()
        .expectBody(TransferQueryResponse.class)
        .returnResult()
        .getResponseBody();

    if (response == null) {
      throw new IllegalStateException("Transfer query response is null");
    }

    return response;
  }

  private TransferResponse transfer(
      String accessToken,
      String idempotencyKey,
      Long fromAccountId,
      Long toAccountId,
      BigDecimal amount
  ) {
    var response = restTestClient.post()
        .uri(uriBuilder -> uriBuilder.path("/transfers/" + idempotencyKey)
            .build())
        .body(new TransferRequest(fromAccountId, toAccountId, amount))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .exchange()
        .expectStatus().isOk()
        .expectBody(TransferResponse.class)
        .returnResult()
        .getResponseBody();

    if (response == null) {
      throw new IllegalStateException("Transfer response is null");
    }

    return response;
  }

  private String issueIdempotencyKey(String accessToken) {
    var response = restTestClient.post()
        .uri("/idempotency-keys")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .exchange()
        .expectStatus().isOk()
        .expectBody(IdempotencyKeyCreateResponse.class)
        .returnResult()
        .getResponseBody();

    if (response == null) {
      throw new IllegalStateException("Idempotency key response is null");
    }

    return response.idempotencyKey();
  }

  private void updateCompletedAt(Long transferId, LocalDateTime completedAt) {
    transactionTemplate.executeWithoutResult(status -> {
      entityManager.createQuery(
              """
                  update TransferEntity t
                     set t.completedAt = :completedAt
                   where t.id = :transferId
                  """
          )
          .setParameter("completedAt", completedAt)
          .setParameter("transferId", transferId)
          .executeUpdate();
      entityManager.flush();
    });
  }

  private record TransferResponse(String status, Long transferId, String errorCode) {

  }
}
