package org.yechan.remittance.transfer.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.yechan.remittance.account.AccountIdentifier;
import org.yechan.remittance.transfer.DailyLimitUsageModel;
import org.yechan.remittance.transfer.DailyLimitUsageProps;
import org.yechan.remittance.transfer.DailyLimitUsageRepository;
import org.yechan.remittance.transfer.TransferProps.TransferScopeValue;

public class DailyLimitUsageRepositoryImpl implements DailyLimitUsageRepository {

  private final DailyLimitUsageJpaRepository repository;

  public DailyLimitUsageRepositoryImpl(DailyLimitUsageJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public DailyLimitUsageModel findOrCreateForUpdate(
      AccountIdentifier identifier,
      TransferScopeValue scope,
      LocalDate usageDate
  ) {
    return repository.findForUpdate(identifier.accountId(), scope, usageDate)
        .orElseGet(() -> (DailyLimitUsageEntity) createAndLock(identifier, scope, usageDate));
  }

  private DailyLimitUsageModel createAndLock(
      AccountIdentifier identifier,
      TransferScopeValue scope,
      LocalDate usageDate
  ) {
    try {
      repository.save(DailyLimitUsageEntity.create(new DailyLimitUsageCreateCommand(
          identifier.accountId(),
          scope,
          usageDate,
          BigDecimal.ZERO
      )));
      repository.flush();
    } catch (DataIntegrityViolationException ignored) {
      // concurrent insert
    }

    return repository.findForUpdate(identifier.accountId(), scope, usageDate)
        .orElseThrow(() -> new IllegalStateException("Daily limit usage not found"));
  }

  private record DailyLimitUsageCreateCommand(
      Long accountId,
      TransferScopeValue scope,
      LocalDate usageDate,
      BigDecimal usedAmount
  ) implements DailyLimitUsageProps {

  }
}
