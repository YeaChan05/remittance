package org.yechan.remittance.account;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yechan.remittance.LoginUserId;
import org.yechan.remittance.account.dto.AccountCreateRequest;
import org.yechan.remittance.account.dto.AccountCreateResponse;
import org.yechan.remittance.account.dto.AccountDeleteResponse;

@RestController
@RequestMapping("/accounts")
record AccountController(
    AccountCreateUseCase accountCreateUseCase,
    AccountDeleteUseCase accountDeleteUseCase
) {

  @PostMapping
  ResponseEntity<AccountCreateResponse> create(
      @LoginUserId Long memberId,
      @RequestBody @Valid AccountCreateRequest request
  ) {
    var account = accountCreateUseCase.create(
        new AccountCreateCommand(memberId, request.bankCode(), request.accountNumber(),
            request.accountName()));
    return ResponseEntity.ok(new AccountCreateResponse(account.accountId(), account.accountName()));
  }

  @DeleteMapping("/{accountId}")
  ResponseEntity<AccountDeleteResponse> delete(
      @LoginUserId Long memberId,
      @PathVariable Long accountId
  ) {
    var account = accountDeleteUseCase.delete(new AccountDeleteCommand(memberId, accountId));
    return ResponseEntity.ok(new AccountDeleteResponse(account.accountId()));
  }

  private record AccountCreateCommand(
      Long memberId,
      String bankCode,
      String accountNumber,
      String accountName
  ) implements AccountProps {

    @Override
    public BigDecimal balance() {
      return BigDecimal.valueOf(0L);
    }

  }

  private record AccountDeleteCommand(long memberId, long accountId) implements AccountDeleteProps {

  }
}
