package org.yechan.remittance.api.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.yechan.remittance.AggregateApplication;
import org.yechan.remittance.TestContainerSetup;
import org.yechan.remittance.TransferTestFixtures;
import org.yechan.remittance.TransferTestFixtures.LedgerRow;
import org.yechan.remittance.TransferTestFixturesConfig;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue;
import org.yechan.remittance.transfer.TransferIdentifier;
import org.yechan.remittance.transfer.TransferModel;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;
import org.yechan.remittance.transfer.TransferQueryCondition;
import org.yechan.remittance.transfer.TransferRepository;
import org.yechan.remittance.transfer.TransferRequestProps;
import org.yechan.remittance.transfer.dto.DepositRequest;
import org.yechan.remittance.transfer.dto.IdempotencyKeyCreateResponse;
import org.yechan.remittance.transfer.dto.TransferRequest;
import org.yechan.remittance.transfer.dto.WithdrawalRequest;

@SpringBootTest(classes = AggregateApplication.class)
@Import({TransferTestFixturesConfig.class, PostSpecs.TransferFailureConfig.class})
public class PostSpecs extends TestContainerSetup {

  private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.01");

  @Autowired
  RestTestClient restTestClient;

  @Autowired
  TransferTestFixtures fixtures;

  @Autowired
  TransferFailureSwitch transferFailureSwitch;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    transferFailureSwitch.disable();
  }

  @Test
  void shouldSucceedTransferAndCreateLedgerOutboxAndIdempotencySnapshot() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);
    var fee = feeFor(transferAmount);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var before = LocalDateTime.now();

    // Act
    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    var after = LocalDateTime.now();

    // Assert
    assertTransferSucceeded(response);
    assertBalance(fromAccount.accountId(), BigDecimal.valueOf(100000L)
        .subtract(transferAmount)
        .subtract(fee));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(80000L));
    assertLedgers(response.transferId(), fromAccount.accountId(), toAccount.accountId(),
        transferAmount.add(fee), transferAmount, before, after);
    assertOutbox(response.transferId(), fromAccount.accountId(), toAccount.accountId(),
        transferAmount);
    assertIdempotency(memberId, idempotencyKey);
  }

  @Test
  void shouldReuseSnapshotOnIdempotentRetry() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );
    var fee = feeFor(transferAmount);

    // Act
    var firstResponse = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    assertTransferSucceeded(firstResponse);
    assertBalance(fromAccount.accountId(), fromAccountBalance
        .subtract(transferAmount)
        .subtract(fee));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(80000L));

    var firstSnapshot = fixtures.loadIdempotencyKey(memberId, idempotencyKey).responseSnapshot();
    var firstLedgers = fixtures.loadLedgers(firstResponse.transferId());
    var firstOutboxes = fixtures.loadOutboxEvents(firstResponse.transferId());

    var secondResponse = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    // Assert
    assertThat(secondResponse.status()).isEqualTo("SUCCEEDED");
    assertThat(secondResponse.transferId()).isEqualTo(firstResponse.transferId());
    assertThat(secondResponse.errorCode()).isNull();
    assertBalance(fromAccount.accountId(), fromAccountBalance
        .subtract(transferAmount)
        .subtract(fee));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(80000L));

    var secondSnapshot = fixtures.loadIdempotencyKey(memberId, idempotencyKey).responseSnapshot();
    assertThat(secondSnapshot).isEqualTo(firstSnapshot);

    var secondLedgers = fixtures.loadLedgers(firstResponse.transferId());
    var secondOutboxes = fixtures.loadOutboxEvents(firstResponse.transferId());
    assertThat(secondLedgers).containsExactlyInAnyOrderElementsOf(firstLedgers);
    assertThat(secondOutboxes).containsExactlyInAnyOrderElementsOf(firstOutboxes);
  }

  @Test
  void shouldRejectIdempotencyKeyWithDifferentBody() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);
    var differentAmount = BigDecimal.valueOf(10000L);
    var fee = feeFor(transferAmount);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var firstResponse = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    assertTransferSucceeded(firstResponse);
    assertBalance(fromAccount.accountId(), fromAccountBalance
        .subtract(transferAmount)
        .subtract(fee));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(80000L));

    var firstSnapshot = fixtures.loadIdempotencyKey(memberId, idempotencyKey).responseSnapshot();
    var firstLedgers = fixtures.loadLedgers(firstResponse.transferId());
    var firstOutboxes = fixtures.loadOutboxEvents(firstResponse.transferId());

    // Act
    restTestClient.post()
        .uri(uriBuilder -> uriBuilder.path("/transfers/" + idempotencyKey)
            .build())
        .body(new TransferRequest(
            fromAccount.accountId(),
            toAccount.accountId(),
            differentAmount
        ))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.auth().accessToken())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        // Assert
        .expectStatus().isBadRequest();

    assertBalance(fromAccount.accountId(), fromAccountBalance
        .subtract(transferAmount)
        .subtract(fee));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(80000L));

    var secondSnapshot = fixtures.loadIdempotencyKey(memberId, idempotencyKey).responseSnapshot();
    assertThat(secondSnapshot).isEqualTo(firstSnapshot);

    var secondLedgers = fixtures.loadLedgers(firstResponse.transferId());
    var secondOutboxes = fixtures.loadOutboxEvents(firstResponse.transferId());
    assertThat(secondLedgers).containsExactlyInAnyOrderElementsOf(firstLedgers);
    assertThat(secondOutboxes).containsExactlyInAnyOrderElementsOf(firstOutboxes);
  }

  @Test
  void shouldFailWhenBalanceIsInsufficient() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(10000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(50000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    // Act
    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    // Assert
    assertThat(response.status()).isEqualTo("FAILED");
    assertThat(response.transferId()).isNull();
    assertThat(response.errorCode()).isEqualTo("INSUFFICIENT_BALANCE");
    assertBalance(fromAccount.accountId(), BigDecimal.valueOf(10000L));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(50000L));

    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore);

    var idempotency = fixtures.loadIdempotencyKey(memberId, idempotencyKey);
    assertThat(idempotency.status()).isEqualTo("FAILED");
    assertThat(idempotency.responseSnapshot()).contains("FAILED", "INSUFFICIENT_BALANCE");
  }

  @Test
  void shouldReturnAcceptedWhenIdempotencyInProgress() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    fixtures.markIdempotencyInProgress(memberId, idempotencyKey, LocalDateTime.now());

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    assertThat(response.status()).isEqualTo("IN_PROGRESS");
    assertThat(response.transferId()).isNull();
    assertThat(response.errorCode()).isNull();
    assertBalance(fromAccount.accountId(), BigDecimal.valueOf(100000L));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(50000L));
    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore);

    var idempotency = fixtures.loadIdempotencyKey(memberId, idempotencyKey);
    assertThat(idempotency.status()).isEqualTo("IN_PROGRESS");
    assertThat(idempotency.responseSnapshot()).isNullOrEmpty();
  }

  @Test
  void shouldFailWhenDailyLimitExceeded() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(10_000_000L);
    var toAccountBalance = BigDecimal.ZERO;
    var firstAmount = BigDecimal.valueOf(2_000_000L);
    var secondAmount = BigDecimal.valueOf(1_200_000L);
    var firstFee = feeFor(firstAmount);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);

    var firstKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );
    var firstResponse = transfer(
        result.auth().accessToken(),
        firstKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        firstAmount
    );

    assertTransferSucceeded(firstResponse);
    assertBalance(fromAccount.accountId(), fromAccountBalance
        .subtract(firstAmount)
        .subtract(firstFee));
    assertBalance(toAccount.accountId(), firstAmount);

    var secondKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    var secondResponse = transfer(
        result.auth().accessToken(),
        secondKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        secondAmount
    );

    assertThat(secondResponse.status()).isEqualTo("FAILED");
    assertThat(secondResponse.transferId()).isNull();
    assertThat(secondResponse.errorCode()).isEqualTo("DAILY_LIMIT_EXCEEDED");
    assertBalance(fromAccount.accountId(), fromAccountBalance
        .subtract(firstAmount)
        .subtract(firstFee));
    assertBalance(toAccount.accountId(), firstAmount);

    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore);

    var idempotency = fixtures.loadIdempotencyKey(
        memberId,
        secondKey,
        IdempotencyScopeValue.TRANSFER
    );
    assertThat(idempotency.status()).isEqualTo("FAILED");
    assertThat(idempotency.responseSnapshot()).contains("FAILED", "DAILY_LIMIT_EXCEEDED");
  }

  @Test
  void shouldFailWhenWithdrawalDailyLimitExceeded() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(2_000_000L);
    var firstAmount = BigDecimal.valueOf(600_000L);
    var secondAmount = BigDecimal.valueOf(500_000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var firstKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.WITHDRAW
    );

    var firstResponse = withdraw(
        result.auth().accessToken(),
        firstKey,
        fromAccount.accountId(),
        firstAmount
    );

    assertTransferSucceeded(firstResponse);
    assertBalance(fromAccount.accountId(), fromAccountBalance.subtract(firstAmount));

    var secondKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.WITHDRAW
    );

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    var secondResponse = withdraw(
        result.auth().accessToken(),
        secondKey,
        fromAccount.accountId(),
        secondAmount
    );

    assertThat(secondResponse.status()).isEqualTo("FAILED");
    assertThat(secondResponse.transferId()).isNull();
    assertThat(secondResponse.errorCode()).isEqualTo("DAILY_LIMIT_EXCEEDED");
    assertBalance(fromAccount.accountId(), fromAccountBalance.subtract(firstAmount));

    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore);

    var idempotency = fixtures.loadIdempotencyKey(
        memberId,
        secondKey,
        IdempotencyScopeValue.WITHDRAW
    );
    assertThat(idempotency.status()).isEqualTo("FAILED");
    assertThat(idempotency.responseSnapshot()).contains("FAILED", "DAILY_LIMIT_EXCEEDED");
  }

  @Test
  void shouldDepositSuccessfully() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var accountBalance = BigDecimal.valueOf(10000L);
    var depositAmount = BigDecimal.valueOf(5000L);

    var account = fixtures.createAccountWithBalance(memberId, "입금", accountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.DEPOSIT
    );

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    var response = deposit(
        result.auth().accessToken(),
        idempotencyKey,
        account.accountId(),
        depositAmount
    );

    assertTransferSucceeded(response);
    assertBalance(account.accountId(), accountBalance.add(depositAmount));
    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore + 1);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore + 1);

    var idempotency = fixtures.loadIdempotencyKey(
        memberId,
        idempotencyKey,
        IdempotencyScopeValue.DEPOSIT
    );
    assertThat(idempotency.status()).isEqualTo("SUCCEEDED");
    assertThat(idempotency.responseSnapshot()).contains("SUCCEEDED");
  }

  @Test
  void shouldFailAfterIdempotencyTimeoutAndWatchdog() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var now = LocalDateTime.now();
    fixtures.markIdempotencyInProgress(memberId, idempotencyKey, now.minusMinutes(10));

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    String timeoutSnapshot = "{\"status\":\"FAILED\",\"errorCode\":\"TIMEOUT\"}";
    fixtures.markIdempotencyTimeoutBefore(now.minusMinutes(5), timeoutSnapshot, now);

    // Act
    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    // Assert
    assertThat(response.status()).isEqualTo("FAILED");
    assertThat(response.transferId()).isNull();
    assertThat(response.errorCode()).isEqualTo("TIMEOUT");
    assertBalance(fromAccount.accountId(), BigDecimal.valueOf(100000L));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(50000L));

    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore);

    var idempotency = fixtures.loadIdempotencyKey(memberId, idempotencyKey);
    assertThat(idempotency.status()).isEqualTo("TIMEOUT");
    assertThat(idempotency.responseSnapshot()).isEqualTo(timeoutSnapshot);
  }

  @Test
  void shouldCreateOutboxEventOnTransfer() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var outboxCountBefore = fixtures.countOutboxEvents();

    // Act
    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    // Assert
    assertTransferSucceeded(response);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore + 1);
    assertOutbox(
        response.transferId(),
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );
  }

  @Test
  void shouldMarkOutboxAsSentAfterPublish() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    // Act
    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    // Assert
    assertTransferSucceeded(response);
    var outboxIds = fixtures.loadOutboxEventIds(response.transferId());
    assertThat(outboxIds).hasSize(1);

    fixtures.markOutboxSent(outboxIds.getFirst());

    var outboxes = fixtures.loadOutboxEvents(response.transferId());
    assertThat(outboxes).hasSize(1);
    assertThat(outboxes.getFirst().status()).isEqualTo("SENT");
  }

  @Test
  void shouldAllowRepublishAfterPublisherCrash() {
    // Arrange
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    // Act
    var response = transfer(
        result.auth().accessToken(),
        idempotencyKey,
        fromAccount.accountId(),
        toAccount.accountId(),
        transferAmount
    );

    // Assert
    assertTransferSucceeded(response);
    var outboxIds = fixtures.loadOutboxEventIds(response.transferId());
    assertThat(outboxIds).hasSize(1);

    var outboxesBefore = fixtures.loadOutboxEvents(response.transferId());
    assertThat(outboxesBefore.getFirst().status()).isEqualTo("NEW");

    // Act
    fixtures.markOutboxSent(outboxIds.getFirst());

    // Assert
    var outboxesAfter = fixtures.loadOutboxEvents(response.transferId());
    assertThat(outboxesAfter).hasSize(1);
    assertThat(outboxesAfter.getFirst().status()).isEqualTo("SENT");
  }

  @Test
  void shouldRollbackTransferWhenPersistenceFails() {
    var result = fixtures.setupAuthentication();

    var memberId = Long.parseLong(result.authentication().getName());
    var fromAccountBalance = BigDecimal.valueOf(100000L);
    var toAccountBalance = BigDecimal.valueOf(50000L);
    var transferAmount = BigDecimal.valueOf(30000L);

    var fromAccount = fixtures.createAccountWithBalance(memberId, "출금", fromAccountBalance);
    var toAccount = fixtures.createAccountWithBalance(memberId, "입금", toAccountBalance);
    var idempotencyKey = issueIdempotencyKey(
        result.auth().accessToken(),
        IdempotencyScopeValue.TRANSFER
    );

    var transferCountBefore = fixtures.countTransfers();
    var outboxCountBefore = fixtures.countOutboxEvents();
    var ledgerCountBefore = fixtures.countLedgers();

    transferFailureSwitch.enable();

    restTestClient.post()
        .uri(uriBuilder -> uriBuilder.path("/transfers/" + idempotencyKey)
            .build())
        .body(new TransferRequest(
            fromAccount.accountId(),
            toAccount.accountId(),
            transferAmount
        ))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.auth().accessToken())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().is5xxServerError();

    assertBalance(fromAccount.accountId(), BigDecimal.valueOf(100000L));
    assertBalance(toAccount.accountId(), BigDecimal.valueOf(50000L));
    assertThat(fixtures.countTransfers()).isEqualTo(transferCountBefore);
    assertThat(fixtures.countOutboxEvents()).isEqualTo(outboxCountBefore);
    assertThat(fixtures.countLedgers()).isEqualTo(ledgerCountBefore);

    var idempotency = fixtures.loadIdempotencyKey(memberId, idempotencyKey);
    assertThat(idempotency.status()).isEqualTo("IN_PROGRESS");
    assertThat(idempotency.responseSnapshot()).isNullOrEmpty();
  }

  private String issueIdempotencyKey(String accessToken) {
    return issueIdempotencyKey(accessToken, null);
  }

  private String issueIdempotencyKey(
      String accessToken,
      IdempotencyScopeValue scope
  ) {
    var response = restTestClient.post()
        .uri(uriBuilder -> uriBuilder.path("/idempotency-keys")
            .queryParamIfPresent("scope", java.util.Optional.ofNullable(scope))
            .build())
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
        .accept(MediaType.APPLICATION_JSON)
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

  private TransferResponse withdraw(
      String accessToken,
      String idempotencyKey,
      Long accountId,
      BigDecimal amount
  ) {
    var response = restTestClient.post()
        .uri(uriBuilder -> uriBuilder.path("/withdrawals/" + idempotencyKey)
            .build())
        .body(new WithdrawalRequest(accountId, amount))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(TransferResponse.class)
        .returnResult()
        .getResponseBody();

    if (response == null) {
      throw new IllegalStateException("Withdrawal response is null");
    }

    return response;
  }

  private TransferResponse deposit(
      String accessToken,
      String idempotencyKey,
      Long accountId,
      BigDecimal amount
  ) {
    var response = restTestClient.post()
        .uri(uriBuilder -> uriBuilder.path("/deposits/" + idempotencyKey)
            .build())
        .body(new DepositRequest(accountId, amount))
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(TransferResponse.class)
        .returnResult()
        .getResponseBody();

    if (response == null) {
      throw new IllegalStateException("Deposit response is null");
    }

    return response;
  }

  private BigDecimal feeFor(BigDecimal amount) {
    return amount.multiply(TRANSFER_FEE_RATE).setScale(2, RoundingMode.DOWN);
  }

  private void assertLedger(
      List<LedgerRow> ledgers,
      Long accountId,
      String side,
      BigDecimal amount,
      LocalDateTime before,
      LocalDateTime after
  ) {
    LedgerRow ledger = ledgers.stream()
        .filter(item -> item.accountId().equals(accountId))
        .filter(item -> item.side().equals(side))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Ledger not found for account " + accountId + " and side " + side));

    assertThat(ledger.amount()).isEqualByComparingTo(amount);
    assertThat(ledger.createdAt()).isAfterOrEqualTo(before);
    assertThat(ledger.createdAt()).isBeforeOrEqualTo(after);
  }

  private void assertTransferSucceeded(TransferResponse response) {
    assertThat(response.status()).isEqualTo("SUCCEEDED");
    assertThat(response.transferId()).isNotNull();
  }

  private void assertBalance(Long accountId, BigDecimal expected) {
    assertThat(fixtures.loadBalance(accountId)).isEqualByComparingTo(expected);
  }

  private void assertLedgers(
      Long transferId,
      Long fromAccountId,
      Long toAccountId,
      BigDecimal debitAmount,
      BigDecimal creditAmount,
      LocalDateTime before,
      LocalDateTime after
  ) {
    var ledgers = fixtures.loadLedgers(transferId);
    assertThat(ledgers).hasSize(2);
    assertLedger(ledgers, fromAccountId, "DEBIT", debitAmount, before, after);
    assertLedger(ledgers, toAccountId, "CREDIT", creditAmount, before, after);
  }

  private void assertOutbox(
      Long transferId,
      Long fromAccountId,
      Long toAccountId,
      BigDecimal amount
  ) {
    var outboxes = fixtures.loadOutboxEvents(transferId);
    assertThat(outboxes).hasSize(1);
    var outbox = outboxes.getFirst();
    assertThat(outbox.status()).isEqualTo("NEW");
    assertThat(outbox.payload()).contains(
        "\"fromAccountId\":" + fromAccountId,
        "\"toAccountId\":" + toAccountId,
        "\"amount\":" + amount
    );
  }

  private void assertIdempotency(Long memberId, String idempotencyKey) {
    var idempotency = fixtures.loadIdempotencyKey(memberId, idempotencyKey);
    assertThat(idempotency.status()).isEqualTo("SUCCEEDED");
    assertThat(idempotency.responseSnapshot()).isNotBlank();
  }

  private record TransferResponse(String status, Long transferId, String errorCode) {

  }

  @TestConfiguration
  static class TransferFailureConfig {

    @Bean
    TransferFailureSwitch transferFailureSwitch() {
      return new TransferFailureSwitch();
    }

    @Bean
    @Primary
    TransferRepository failureTransferRepository(
        @Qualifier("transferRepository") TransferRepository delegate,
        TransferFailureSwitch transferFailureSwitch
    ) {
      return new FailureTransferRepository(delegate, transferFailureSwitch);
    }
  }

  static class TransferFailureSwitch {

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    void enable() {
      enabled.set(true);
    }

    void disable() {
      enabled.set(false);
    }

    boolean shouldFail() {
      return enabled.get();
    }
  }

  private record FailureTransferRepository(TransferRepository delegate,
                                           TransferFailureSwitch failureSwitch)
      implements TransferRepository {

    @Override
    public TransferModel save(TransferRequestProps props) {
      if (failureSwitch.shouldFail()) {
        throw new IllegalStateException("Transfer save failed");
      }
      return delegate.save(props);
    }

    @Override
    public Optional<TransferModel> findById(TransferIdentifier identifier) {
      return delegate.findById(identifier);
    }

    @Override
    public List<? extends TransferModel> findCompletedByAccountId(
        AccountIdentifier identifier,
        TransferQueryCondition condition
    ) {
      return delegate.findCompletedByAccountId(identifier, condition);
    }

    @Override
    public BigDecimal sumAmountByFromAccountIdAndScopeBetween(AccountIdentifier identifier,
        TransferScopeValue scope, LocalDateTime from, LocalDateTime to) {
      return delegate.sumAmountByFromAccountIdAndScopeBetween(identifier, scope, from, to);
    }
  }
}
