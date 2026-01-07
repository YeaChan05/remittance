package org.yechan.remittance.member;

import org.yechan.remittance.BusinessException;

public class MemberException extends BusinessException {

  MemberException(String message) {
    super(message);
  }
}
