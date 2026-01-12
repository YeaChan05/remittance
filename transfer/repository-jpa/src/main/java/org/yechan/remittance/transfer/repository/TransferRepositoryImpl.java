package org.yechan.remittance.transfer.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.transfer.TransferIdentifier;
import org.yechan.remittance.transfer.TransferModel;
import org.yechan.remittance.transfer.TransferProps;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;
import org.yechan.remittance.transfer.TransferQueryCondition;
import org.yechan.remittance.transfer.TransferRepository;
import org.yechan.remittance.transfer.TransferRequestProps;

public class TransferRepositoryImpl implements TransferRepository {

  private static final List<TransferStatusValue> COMPLETED_STATUSES = List.of(
      TransferStatusValue.SUCCEEDED,
      TransferStatusValue.FAILED
  );

  private final TransferJpaRepository repository;

  public TransferRepositoryImpl(TransferJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public TransferModel save(TransferRequestProps props) {
    var command = new TransferCreateCommand(props);
    return repository.save(TransferEntity.create(command));
  }

  @Override
  public Optional<TransferModel> findById(TransferIdentifier identifier) {
    return repository.findById(identifier.transferId())
        .map(transfer -> transfer);
  }

  @Override
  public List<? extends TransferModel> findCompletedByAccountId(
      AccountIdentifier identifier,
      TransferQueryCondition condition
  ) {
    Pageable pageable = condition.limit() == null
        ? Pageable.unpaged()
        : PageRequest.of(0, condition.limit());

    return repository.findCompletedByAccountId(
        identifier.accountId(),
        COMPLETED_STATUSES,
        condition.from(),
        condition.to(),
        pageable
    );
  }

  @Override
  public BigDecimal sumAmountByFromAccountIdAndScopeBetween(
      AccountIdentifier identifier,
      TransferScopeValue scope,
      LocalDateTime from,
      LocalDateTime to
  ) {
    return repository.sumAmountByFromAccountIdAndScopeBetween(
        identifier.accountId(),
        scope,
        TransferStatusValue.SUCCEEDED,
        from,
        to
    );
  }

  private record TransferCreateCommand(TransferRequestProps props) implements TransferProps {

    @Override
    public Long fromAccountId() {
      return props.fromAccountId();
    }

    @Override
    public Long toAccountId() {
      return props.toAccountId();
    }

    @Override
    public BigDecimal amount() {
      return props.amount();
    }

    @Override
    public TransferScopeValue scope() {
      return props.scope();
    }

    @Override
    public TransferStatusValue status() {
      return TransferStatusValue.SUCCEEDED;
    }

    @Override
    public LocalDateTime requestedAt() {
      return LocalDateTime.now();
    }

    @Override
    public LocalDateTime completedAt() {
      return LocalDateTime.now();
    }
  }
}
