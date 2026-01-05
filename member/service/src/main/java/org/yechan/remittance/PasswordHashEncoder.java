package org.yechan.remittance;

public interface PasswordHashEncoder {

  String encode(String password);

  boolean matches(String password, String encodedPassword);
}
