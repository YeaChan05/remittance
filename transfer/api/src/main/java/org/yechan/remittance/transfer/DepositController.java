package org.yechan.remittance.transfer;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.LoginUserId;
import org.yechan.remittance.transfer.dto.DepositRequest;

@RestController
@RequestMapping("/deposits")
record DepositController(
    TransferCreateUseCase transferCreateUseCase
) {

  @PostMapping("/{idempotencyKey}")
  public TransferResult deposit(
      @LoginUserId Long memberId,
      @PathVariable String idempotencyKey,
      @RequestBody @Valid DepositRequest request
  ) {
    return transferCreateUseCase.transfer(memberId, idempotencyKey, request);
  }
}
