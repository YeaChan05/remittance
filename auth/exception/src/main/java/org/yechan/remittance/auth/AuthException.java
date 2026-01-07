package org.yechan.remittance.auth;

import org.yechan.remittance.BusinessException;
import org.yechan.remittance.Status;

public class AuthException extends BusinessException {

  public AuthException(Status status, String message) {
    super(status, message);
  }
}
