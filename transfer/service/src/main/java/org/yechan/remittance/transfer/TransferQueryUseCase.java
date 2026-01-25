package org.yechan.remittance.transfer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.yechan.remittance.account.AccountRepository;

public interface TransferQueryUseCase {

  List<? extends TransferModel> query(
      Long memberId,
      Long accountId,
      TransferQueryCondition condition
  );
}

@Slf4j
record TransferQueryService(
    AccountRepository accountRepository,
    TransferRepository transferRepository
) implements TransferQueryUseCase {

  @Override
  public List<? extends TransferModel> query(
      Long memberId,
      Long accountId,
      TransferQueryCondition condition
  ) {
     log.info("transfer.query.start memberId={} accountId={}", memberId, accountId);
    var account = accountRepository.findById(() -> accountId)
        .orElseThrow(() -> {
           log.warn("transfer.query.account_not_found accountId={}", accountId);
          return new TransferFailedException(
              TransferFailureCode.ACCOUNT_NOT_FOUND,
              "Account not found"
          );
        });

    if (!memberId.equals(account.memberId())) {
       log.warn("transfer.query.owner_mismatch memberId={} accountId={}", memberId, accountId);
      throw new TransferFailedException(TransferFailureCode.INVALID_REQUEST,
          "Account owner mismatch");
    }

     log.info("transfer.query.fetch accountId={}", accountId);
    return transferRepository.findCompletedByAccountId(() -> accountId, condition);
  }
}
