package org.yechan.remittance;

public class BusinessException extends RuntimeException {

  private final Status status;

  protected BusinessException(Status status, String message) {
    super(message);
    this.status = status;
  }

  public BusinessException(String message) {
    super(message);
    this.status = Status.INTERNAL_SERVER_ERROR;
  }

  public Status getStatus() {
    return status;
  }
}
