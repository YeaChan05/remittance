package org.yechan.remittance.transfer;

import java.util.List;

public interface OutboxEventRepository {

  OutboxEventModel save(OutboxEventProps props);

  List<? extends OutboxEventModel> findNewForPublish(Integer limit);

  void markSent(OutboxEventIdentifier identifier);
}
