package org.yechan.remittance;

public class AuthInvalidCredentialException extends AuthException {

  public AuthInvalidCredentialException(String message) {
    super(Status.AUTHENTICATION_FAILED, message);
  }
}
