package org.yechan.remittance.member;

import org.yechan.remittance.BusinessException;
import org.yechan.remittance.Status;

public class MemberPermissionDeniedException extends BusinessException {

  public MemberPermissionDeniedException(String message) {
    super(message);
  }

  public MemberPermissionDeniedException(Status status, String message) {
    super(status, message);
  }
}
