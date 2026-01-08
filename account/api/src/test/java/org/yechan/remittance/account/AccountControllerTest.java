package org.yechan.remittance.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.yechan.remittance.account.dto.AccountCreateRequest;

class AccountControllerTest {

  @Test
  void createAccountReturnsResponse() {
    AccountCreateUseCase createUseCase = props -> new Account(
        11L,
        props.memberId(),
        props.bankCode(),
        props.accountNumber(),
        props.accountName(),
        props.balance()
    );
    AccountDeleteUseCase deleteUseCase = props -> new Account(
        props.accountId(),
        props.memberId(),
        "090",
        "123-456",
        "생활비",
        BigDecimal.ZERO
    );
    var controller = new AccountController(createUseCase, deleteUseCase);

    var response = controller.create(1L, new AccountCreateRequest("090", "123-456", "생활비"));

    assertNotNull(response.getBody());
    assertEquals(11L, response.getBody().accountId());
    assertEquals("생활비", response.getBody().accountName());
  }

  @Test
  void deleteAccountReturnsResponse() {
    AccountCreateUseCase createUseCase = props -> new Account(
        11L,
        props.memberId(),
        props.bankCode(),
        props.accountNumber(),
        props.accountName(),
        BigDecimal.ZERO
    );
    AccountDeleteUseCase deleteUseCase = props -> new Account(
        props.accountId(),
        props.memberId(),
        "090",
        "123-456",
        "생활비",
        BigDecimal.ZERO
    );
    var controller = new AccountController(createUseCase, deleteUseCase);

    var response = controller.delete(1L, 11L);

    assertNotNull(response.getBody());
    assertEquals(11L, response.getBody().accountId());
  }
}
