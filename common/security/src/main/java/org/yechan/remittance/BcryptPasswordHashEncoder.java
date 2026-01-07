package org.yechan.remittance;

import org.springframework.security.crypto.password.PasswordEncoder;

public class BcryptPasswordHashEncoder implements PasswordHashEncoder {

  private final PasswordEncoder passwordEncoder;

  public BcryptPasswordHashEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public String encode(String password) {
    return passwordEncoder.encode(password);
  }

  @Override
  public boolean matches(String password, String encodedPassword) {
    return passwordEncoder.matches(password, encodedPassword);
  }
}
