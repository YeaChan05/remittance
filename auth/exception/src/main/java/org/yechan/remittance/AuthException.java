package org.yechan.remittance;

public class AuthException extends BusinessException {

  public AuthException(Status status, String message) {
    super(status, message);
  }
}
