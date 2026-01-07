package org.yechan.remittance;

public enum Status {
  BAD_REQUEST,
  RESOURCE_NOT_FOUND,
  INTERNAL_SERVER_ERROR,
  AUTHENTICATION_FAILED;

  public int toHttpStatus() {
    return switch (this) {
      case BAD_REQUEST -> 400;
      case RESOURCE_NOT_FOUND -> 404;
      case AUTHENTICATION_FAILED -> 401;
      case INTERNAL_SERVER_ERROR -> 500;
    };
  }
}
