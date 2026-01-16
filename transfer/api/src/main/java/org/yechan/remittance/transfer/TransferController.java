package org.yechan.remittance.transfer;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.LoginUserId;
import org.yechan.remittance.transfer.dto.TransferQueryResponse;
import org.yechan.remittance.transfer.dto.TransferRequest;

@RequestMapping("/transfers")
@RestController
record TransferController(
    TransferCreateUseCase transferCreateUseCase,
    TransferQueryUseCase transferQueryUseCase
) implements TransferApi {

  @Override
  @PostMapping("/{idempotencyKey}")
  public TransferResult transfer(
      @LoginUserId Long memberId,
      @PathVariable String idempotencyKey,
      @RequestBody TransferRequest props) {
    return transferCreateUseCase.transfer(memberId, idempotencyKey, props);
  }

  @Override
  @GetMapping
  public TransferQueryResponse query(
      @LoginUserId Long memberId,
      @RequestParam Long accountId,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) Integer limit
  ) {
    List<? extends TransferModel> transfers = transferQueryUseCase.query(
        memberId,
        accountId,
        new TransferQueryCondition(from, to, limit)
    );
    return TransferQueryResponse.from(transfers);
  }
}
