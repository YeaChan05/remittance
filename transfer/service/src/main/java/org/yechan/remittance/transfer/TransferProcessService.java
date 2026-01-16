package org.yechan.remittance.transfer;

import static org.yechan.remittance.transfer.TransferFailureCode.ACCOUNT_NOT_FOUND;
import static org.yechan.remittance.transfer.TransferFailureCode.DAILY_LIMIT_EXCEEDED;
import static org.yechan.remittance.transfer.TransferFailureCode.INSUFFICIENT_BALANCE;
import static org.yechan.remittance.transfer.TransferFailureCode.INVALID_REQUEST;
import static org.yechan.remittance.transfer.TransferSnapshotUtil.toOutboxPayload;
import static org.yechan.remittance.transfer.TransferSnapshotUtil.toSnapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.yechan.remittance.account.AccountModel;
import org.yechan.remittance.account.AccountRepository;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

class TransferProcessService {

  private static final BigDecimal WITHDRAW_DAILY_LIMIT = BigDecimal.valueOf(1_000_000);
  private static final BigDecimal TRANSFER_DAILY_LIMIT = BigDecimal.valueOf(3_000_000);
  private static final String AGGREGATE_TYPE = "TRANSFER";
  private static final String EVENT_TYPE = "TRANSFER_COMPLETED";

  private final AccountRepository accountRepository;
  private final TransferRepository transferRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final IdempotencyKeyRepository idempotencyKeyRepository;
  private final DailyLimitUsageRepository dailyLimitUsageRepository;

  public TransferProcessService(
      AccountRepository accountRepository,
      TransferRepository transferRepository,
      OutboxEventRepository outboxEventRepository,
      IdempotencyKeyRepository idempotencyKeyRepository,
      DailyLimitUsageRepository dailyLimitUsageRepository
  ) {
    this.accountRepository = accountRepository;
    this.transferRepository = transferRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.idempotencyKeyRepository = idempotencyKeyRepository;
    this.dailyLimitUsageRepository = dailyLimitUsageRepository;
  }

  @Transactional
  public TransferResult process(
      Long memberId,
      String idempotencyKey,
      TransferRequestProps props,
      LocalDateTime now
  ) {
    AccountPair accounts = lockAccounts(props);
    validateOwner(memberId, accounts);
    validateDailyLimit(props, now);
    validateBalance(props, accounts);
    updateBalances(props, accounts);
    return persistTransfer(memberId, idempotencyKey, props, now);
  }

  private AccountPair lockAccounts(TransferRequestProps props) {
    Long fromAccountId = props.fromAccountId();
    Long toAccountId = props.toAccountId();
    if (props.scope() == TransferScopeValue.WITHDRAW
        || props.scope() == TransferScopeValue.DEPOSIT) {
      AccountModel fromAccount = getAccountForUpdate(fromAccountId);
      return new AccountPair(fromAccount, fromAccount);
    }
    if (fromAccountId.equals(toAccountId)) {
      throw new TransferFailedException(INVALID_REQUEST, "Same account");
    }

    AccountModel firstAccount = getAccountForUpdate(Math.min(fromAccountId, toAccountId));
    AccountModel secondAccount = getAccountForUpdate(Math.max(fromAccountId, toAccountId));

    AccountModel fromAccount = fromAccountId.equals(firstAccount.accountId())
        ? firstAccount : secondAccount;
    AccountModel toAccount = toAccountId.equals(firstAccount.accountId())
        ? firstAccount : secondAccount;

    return new AccountPair(fromAccount, toAccount);
  }

  private AccountModel getAccountForUpdate(Long accountId) {
    return accountRepository.findByIdForUpdate(() -> accountId)
        .orElseThrow(() -> new TransferFailedException(ACCOUNT_NOT_FOUND, "Account not found"));
  }

  private void validateOwner(Long memberId, AccountPair accounts) {
    if (!memberId.equals(accounts.fromAccount().memberId())) {
      throw new TransferFailedException(INVALID_REQUEST, "Account owner mismatch");
    }
  }

  private void validateBalance(TransferRequestProps props, AccountPair accounts) {
    if (props.scope() == TransferScopeValue.DEPOSIT) {
      return;
    }

    BigDecimal debitAmount = props.amount().add(props.fee());
    if (accounts.fromAccount().balance().compareTo(debitAmount) < 0) {
      throw new TransferFailedException(INSUFFICIENT_BALANCE, "Insufficient balance");
    }
  }

  private void validateDailyLimit(TransferRequestProps props, LocalDateTime now) {
    if (props.scope() == TransferScopeValue.DEPOSIT) {
      return;
    }
    DailyLimitUsageModel usage = dailyLimitUsageRepository.findOrCreateForUpdate(
        props::fromAccountId,
        props.scope(),
        now.toLocalDate()
    );

    BigDecimal limit = props.scope() == TransferScopeValue.WITHDRAW
        ? WITHDRAW_DAILY_LIMIT
        : TRANSFER_DAILY_LIMIT;

    BigDecimal nextUsed = usage.usedAmount().add(props.amount());
    if (nextUsed.compareTo(limit) > 0) {
      throw new TransferFailedException(DAILY_LIMIT_EXCEEDED, "Daily limit exceeded");
    }

    usage.updateUsedAmount(nextUsed);
  }

  private void updateBalances(TransferRequestProps props, AccountPair accounts) {
    if (props.scope() == TransferScopeValue.DEPOSIT) {
      BigDecimal updatedToBalance = accounts.toAccount().balance().add(props.amount());
      accounts.toAccount().updateBalance(updatedToBalance);
      return;
    }

    BigDecimal debitAmount = props.amount().add(props.fee());
    BigDecimal updatedFromBalance = accounts.fromAccount().balance().subtract(debitAmount);
    accounts.fromAccount().updateBalance(updatedFromBalance);

    if (props.scope() == TransferScopeValue.WITHDRAW) {
      return;
    }

    BigDecimal updatedToBalance = accounts.toAccount().balance().add(props.amount());
    accounts.toAccount().updateBalance(updatedToBalance);
  }

  private TransferResult persistTransfer(
      Long memberId,
      String idempotencyKey,
      TransferRequestProps props,
      LocalDateTime now
  ) {
    TransferModel transfer = transferRepository.save(props);
    if (props.scope() == TransferScopeValue.TRANSFER) {
      outboxEventRepository.save(new OutboxEventCreateCommand(transfer, props, now));
    }

    TransferResult result = TransferResult.succeeded(transfer.transferId());
    idempotencyKeyRepository.markSucceeded(
        memberId,
        toIdempotencyScope(props.scope()),
        idempotencyKey,
        toSnapshot(result),
        now
    );

    return result;
  }

  private IdempotencyKeyProps.IdempotencyScopeValue toIdempotencyScope(
      TransferScopeValue scope
  ) {
    return switch (scope) {
      case TRANSFER -> IdempotencyKeyProps.IdempotencyScopeValue.TRANSFER;
      case WITHDRAW -> IdempotencyKeyProps.IdempotencyScopeValue.WITHDRAW;
      case DEPOSIT -> IdempotencyKeyProps.IdempotencyScopeValue.DEPOSIT;
    };
  }

  private record OutboxEventCreateCommand(
      TransferModel transfer,
      TransferRequestProps props,
      LocalDateTime now
  ) implements OutboxEventProps {

    @Override
    public String aggregateType() {
      return AGGREGATE_TYPE;
    }

    @Override
    public String aggregateId() {
      return transfer.transferId().toString();
    }

    @Override
    public String eventType() {
      return EVENT_TYPE;
    }

    @Override
    public String payload() {
      return toOutboxPayload(transfer, props, now);
    }

    @Override
    public OutboxEventStatusValue status() {
      return OutboxEventStatusValue.NEW;
    }
  }

  private record AccountPair(AccountModel fromAccount, AccountModel toAccount) {

  }
}
