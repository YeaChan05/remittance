package org.yechan.remittance.transfer.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.yechan.remittance.transfer.OutboxEventIdentifier;
import org.yechan.remittance.transfer.OutboxEventModel;
import org.yechan.remittance.transfer.OutboxEventProps;
import org.yechan.remittance.transfer.OutboxEventProps.OutboxEventStatusValue;
import org.yechan.remittance.transfer.OutboxEventRepository;

public class OutboxEventRepositoryImpl implements OutboxEventRepository {

  private final OutboxEventJpaRepository repository;

  public OutboxEventRepositoryImpl(OutboxEventJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public OutboxEventModel save(OutboxEventProps props) {
    return repository.save(OutboxEventEntity.create(props));
  }

  @Override
  public List<? extends OutboxEventModel> findNewForPublish(Integer limit) {
    Pageable pageable = limit == null ? Pageable.unpaged() : PageRequest.of(0, limit);
    return repository.findNewForPublish(
        OutboxEventStatusValue.NEW,
        null,
        pageable
    );
  }

  @Override
  public void markSent(OutboxEventIdentifier identifier) {
    repository.markSent(identifier.eventId());
  }
}
