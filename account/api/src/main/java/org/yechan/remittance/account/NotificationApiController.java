package org.yechan.remittance.account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yechan.remittance.LoginUserId;

@RestController
@RequestMapping("/notification")
record NotificationApiController(NotificationUseCase useCase) implements NotificationApi {

  @Override
  @GetMapping("/subscribe")
  public SseEmitter connect(@LoginUserId Long memberId) {
    return useCase.connectRegister(memberId);
  }
}
