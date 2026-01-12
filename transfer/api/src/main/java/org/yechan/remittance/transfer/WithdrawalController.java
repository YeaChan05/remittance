package org.yechan.remittance.transfer;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.LoginUserId;
import org.yechan.remittance.transfer.dto.WithdrawalRequest;

@RestController
@RequestMapping("/withdrawals")
record WithdrawalController(
    TransferCreateUseCase transferCreateUseCase
) {

  @PostMapping("/{idempotencyKey}")
  public TransferResult withdraw(
      @LoginUserId Long memberId,
      @PathVariable String idempotencyKey,
      @RequestBody @Valid WithdrawalRequest request
  ) {
    return transferCreateUseCase.transfer(memberId, idempotencyKey, request);
  }
}
