package org.yechan.remittance.transfer;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.LoginUserId;
import org.yechan.remittance.transfer.dto.TransferRequest;

@RequestMapping("/transfers")
@RestController
record TransferController(
    TransferCreateUseCase transferCreateUseCase
) {

  @PostMapping("/{idempotencyKey}")
  TransferResult transfer(
      @LoginUserId Long memberId,
      @PathVariable String idempotencyKey,
      @RequestBody TransferRequest props) {
    return transferCreateUseCase.transfer(memberId, idempotencyKey, props);
  }
}
