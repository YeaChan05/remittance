package org.yechan.remittance.dto;

import jakarta.validation.constraints.Pattern;

public record MemberRegisterRequest(String name,

                                    @Pattern(
                                        regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                                        message = "Invalid email address"
                                    )
                                    String email,

                                    @Pattern(
                                        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z\\d!@#$%^&*]{8,}$",
                                        message = "Invalid password format. Password must contain at least one letter, one number, and one special character.")
                                    String password) {

}
