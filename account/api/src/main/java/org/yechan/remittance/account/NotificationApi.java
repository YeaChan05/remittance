package org.yechan.remittance.account;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationApi {

  SseEmitter connect(Long memberId);
}
