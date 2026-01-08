package org.yechan.remittance.transfer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.transfer.TransferIdentifier;
import org.yechan.remittance.transfer.TransferModel;
import org.yechan.remittance.transfer.TransferProps;
import org.yechan.remittance.transfer.TransferProps.TransferStatusValue;
import org.yechan.remittance.transfer.TransferQueryCondition;
import org.yechan.remittance.transfer.TransferRepository;

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
  public TransferModel save(TransferProps props) {
    return repository.save(TransferEntity.create(props));
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
}
