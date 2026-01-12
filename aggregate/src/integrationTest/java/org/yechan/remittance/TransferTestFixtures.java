package org.yechan.remittance;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.support.TransactionTemplate;
import org.yechan.remittance.account.AccountProps;
import org.yechan.remittance.member.dto.MemberLoginRequest;
import org.yechan.remittance.member.dto.MemberLoginResponse;
import org.yechan.remittance.member.dto.MemberRegisterRequest;

public class TransferTestFixtures {

  private final RestTestClient restTestClient;
  private final EntityManager em;
  private final TransactionTemplate transactionTemplate;
  private final TokenVerifier tokenVerifier;

  public TransferTestFixtures(
      RestTestClient restTestClient,
      EntityManager em,
      TransactionTemplate transactionTemplate,
      TokenVerifier tokenVerifier
  ) {
    this.restTestClient = restTestClient;
    this.em = em;
    this.transactionTemplate = transactionTemplate;
    this.tokenVerifier = tokenVerifier;
  }

  public AccountSeed createAccountWithBalance(
      Long memberId,
      String accountName,
      BigDecimal balance
  ) {
    Object account = createAccountEntity(memberId, accountName, balance);
    transactionTemplate.executeWithoutResult(status -> {
      em.persist(account);
      em.flush();
    });

    Long accountId = ReflectionTestUtils.invokeMethod(account, "accountId");
    String bankCode = ReflectionTestUtils.invokeMethod(account, "bankCode");
    String accountNumber = ReflectionTestUtils.invokeMethod(account, "accountNumber");
    String savedAccountName = ReflectionTestUtils.invokeMethod(account, "accountName");
    BigDecimal savedBalance = ReflectionTestUtils.invokeMethod(account, "balance");

    return new AccountSeed(
        accountId,
        bankCode,
        accountNumber,
        savedAccountName,
        savedBalance);
  }

  private Object createAccountEntity(
      Long memberId,
      String accountName,
      BigDecimal balance
  ) {
    try {
      Class<?> accountEntityClass = Class.forName(
          "org.yechan.remittance.account.repository.AccountEntity"
      );
      return ReflectionTestUtils.invokeMethod(
          accountEntityClass,
          "create",
          new AccountProps() {
            @Override
            public Long memberId() {
              return memberId;
            }

            @Override
            public String bankCode() {
              return "001";
            }

            @Override
            public String accountNumber() {
              return String.valueOf(System.currentTimeMillis());
            }

            @Override
            public String accountName() {
              return accountName;
            }

            @Override
            public BigDecimal balance() {
              return balance;
            }
          }
      );
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("AccountEntity class not found", e);
    }
  }

//  public MemberEntity createMember(String name, String email, String passwordHash) {
//    try {
//      return MemberEntity.class.getDeclaredConstructor(String.class, String.class, String.class)
//          .newInstance(name, email, passwordHash);
//    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
//             NoSuchMethodException e) {
//      throw new RuntimeException(e);
//    }
//  }

  public AuthSeed registerAndIssueToken(String name) {
    String email = EmailGenerator.generate();
    String password = PasswordGenerator.generate();

    restTestClient.post()
        .uri("/members")
        .body(new MemberRegisterRequest(name, email, password))
        .exchange()
        .expectStatus().isOk();

    var response = restTestClient.post()
        .uri("/login")
        .body(new MemberLoginRequest(email, password))
        .exchange()
        .expectStatus().isOk()
        .expectBody(MemberLoginResponse.class)
        .returnResult()
        .getResponseBody();

    if (response == null) {
      throw new IllegalStateException("Login response is null");
    }

    return new AuthSeed(response.accessToken(), email, password);
  }

  public IdempotencyRow loadIdempotencyKey(Long memberId, String idempotencyKey) {
    return loadIdempotencyKey(memberId, idempotencyKey,
        org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue.TRANSFER);
  }

  public IdempotencyRow loadIdempotencyKey(
      Long memberId,
      String idempotencyKey,
      org.yechan.remittance.transfer.IdempotencyKeyProps.IdempotencyScopeValue scope
  ) {
    List<Object[]> rows = em.createQuery(
            """
                select i.status, i.responseSnapshot
                  from IdempotencyKeyEntity i
                 where i.memberId = :memberId
                   and i.scope = :scope
                   and i.idempotencyKey = :idempotencyKey
                """,
            Object[].class
        )
        .setParameter("memberId", memberId)
        .setParameter("scope", scope)
        .setParameter("idempotencyKey", idempotencyKey)
        .getResultList();
    if (rows.isEmpty()) {
      throw new IllegalStateException("Idempotency key not found");
    }
    Object[] row = rows.getFirst();
    return new IdempotencyRow(row[0].toString(), (String) row[1]);
  }

  public List<OutboxRow> loadOutboxEvents(Long transferId) {
    List<Object[]> rows = em.createQuery(
            """
                select o.status, o.payload
                  from OutboxEventEntity o
                 where o.aggregateType = :aggregateType
                   and o.aggregateId = :aggregateId
                """,
            Object[].class
        )
        .setParameter("aggregateType", "TRANSFER")
        .setParameter("aggregateId", transferId.toString())
        .getResultList();
    return rows.stream()
        .map(row -> new OutboxRow(
            row[0].toString(),
            (String) row[1]
        ))
        .toList();
  }

  public List<Long> loadOutboxEventIds(Long transferId) {
    return em.createQuery(
            """
                select o.id
                  from OutboxEventEntity o
                 where o.aggregateType = :aggregateType
                   and o.aggregateId = :aggregateId
                """,
            Long.class
        )
        .setParameter("aggregateType", "TRANSFER")
        .setParameter("aggregateId", transferId.toString())
        .getResultList();
  }

  public void markOutboxSent(Long eventId) {
    transactionTemplate.executeWithoutResult(status -> {
      em.createQuery(
              """
                  update OutboxEventEntity o
                     set o.status = org.yechan.remittance.transfer.OutboxEventProps$OutboxEventStatusValue.SENT
                   where o.id = :eventId
                  """
          )
          .setParameter("eventId", eventId)
          .executeUpdate();
      em.flush();
    });
  }

  public long countOutboxEvents() {
    Long count = em.createQuery(
            "select count(o) from OutboxEventEntity o",
            Long.class
        )
        .getSingleResult();
    return count == null ? 0L : count;
  }

  public BigDecimal loadBalance(Long accountId) {
    BigDecimal balance = em.createQuery(
            "select a.balance from AccountEntity a where a.id = :accountId",
            BigDecimal.class
        )
        .setParameter("accountId", accountId)
        .getSingleResult();
    if (balance == null) {
      throw new IllegalStateException("Balance not found");
    }
    return balance;
  }

  public List<LedgerRow> loadLedgers(Long transferId) {
    List<Object[]> rows = em.createQuery(
            """
                select l.accountId, l.amount, l.side, l.createdAt
                  from LedgerEntity l
                 where l.transferId = :transferId
                """,
            Object[].class
        )
        .setParameter("transferId", transferId)
        .getResultList();
    return rows.stream()
        .map(row -> new LedgerRow(
            ((Number) row[0]).longValue(),
            (BigDecimal) row[1],
            row[2].toString(),
            (LocalDateTime) row[3]
        ))
        .toList();
  }

  public long countLedgers() {
    Long count = em.createQuery(
            "select count(l) from LedgerEntity l",
            Long.class
        )
        .getSingleResult();
    return count == null ? 0L : count;
  }

  public long countTransfers() {
    Long count = em.createQuery(
            "select count(t) from TransferEntity t",
            Long.class
        )
        .getSingleResult();
    return count == null ? 0L : count;
  }

  public void markIdempotencyInProgress(
      Long memberId,
      String idempotencyKey,
      LocalDateTime startedAt
  ) {
    transactionTemplate.executeWithoutResult(status -> {
      em.createQuery(
              """
                  update IdempotencyKeyEntity i
                     set i.status = org.yechan.remittance.transfer.IdempotencyKeyProps$IdempotencyKeyStatusValue.IN_PROGRESS,
                         i.startedAt = :startedAt
                   where i.memberId = :memberId
                     and i.scope = org.yechan.remittance.transfer.IdempotencyKeyProps$IdempotencyScopeValue.TRANSFER
                     and i.idempotencyKey = :idempotencyKey
                  """
          )
          .setParameter("memberId", memberId)
          .setParameter("idempotencyKey", idempotencyKey)
          .setParameter("startedAt", startedAt)
          .executeUpdate();
      em.flush();
    });
  }

  public int markIdempotencyTimeoutBefore(
      LocalDateTime cutoff,
      String responseSnapshot,
      LocalDateTime completedAt
  ) {
    return transactionTemplate.execute(status -> {
      int updated = em.createQuery(
              """
                  update IdempotencyKeyEntity i
                     set i.status = org.yechan.remittance.transfer.IdempotencyKeyProps$IdempotencyKeyStatusValue.TIMEOUT,
                         i.responseSnapshot = :responseSnapshot,
                         i.completedAt = :completedAt
                   where i.status = org.yechan.remittance.transfer.IdempotencyKeyProps$IdempotencyKeyStatusValue.IN_PROGRESS
                     and i.startedAt < :cutoff
                  """
          )
          .setParameter("cutoff", cutoff)
          .setParameter("responseSnapshot", responseSnapshot)
          .setParameter("completedAt", completedAt)
          .executeUpdate();
      em.flush();
      return updated;
    });
  }

  public Result setupAuthentication() {
    var auth = registerAndIssueToken("tester");
    var authentication = tokenVerifier.verify(auth.accessToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return new Result(auth, authentication);
  }

  public record AccountSeed(
      Long accountId,
      String bankCode,
      String accountNumber,
      String accountName,
      BigDecimal balance
  ) {

  }

  public record AuthSeed(String accessToken, String email, String password) {

  }

  public record LedgerRow(
      Long accountId,
      BigDecimal amount,
      String side,
      LocalDateTime createdAt
  ) {

  }

  public record OutboxRow(String status, String payload) {

  }

  public record IdempotencyRow(String status, String responseSnapshot) {

  }

  public record Result(AuthSeed auth, Authentication authentication) {

  }
}
