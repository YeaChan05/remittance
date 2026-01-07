package org.yechan.remittance.account.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountCreateRequest(
    @NotBlank(message = "Invalid bank code")
    String bankCode,
    @NotBlank(message = "Invalid account number")
    String accountNumber,
    @NotBlank(message = "Invalid account name")
    String accountName
) {

}
