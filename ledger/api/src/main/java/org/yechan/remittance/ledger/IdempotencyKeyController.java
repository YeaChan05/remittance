package org.yechan.remittance.ledger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.LoginUserId;
import org.yechan.remittance.ledger.dto.IdempotencyKeyCreateResponse;

@RestController
@RequestMapping("/idempotency-keys")
record IdempotencyKeyController(
    IdempotencyKeyCreateUseCase idempotencyKeyCreateUseCase
) {

  @PostMapping
  ResponseEntity<IdempotencyKeyCreateResponse> create(@LoginUserId Long memberId) {
    var created = idempotencyKeyCreateUseCase.create(new IdempotencyKeyCreateCommand(memberId));
    return ResponseEntity.ok(
        new IdempotencyKeyCreateResponse(created.idempotencyKey(), created.expiresAt()));
  }

  private record IdempotencyKeyCreateCommand(long memberId) implements IdempotencyKeyCreateProps {

  }
}
