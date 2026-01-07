package org.yechan.remittance.auth;

import org.yechan.remittance.Status;

public class AuthInvalidCredentialException extends AuthException {

  public AuthInvalidCredentialException(String message) {
    super(Status.AUTHENTICATION_FAILED, message);
  }
}
