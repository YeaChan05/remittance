package org.yechan.remittance.account;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationApiControllerTest {

  @Test
  void connectRegistersEmitter() {
    var registry = new NotificationSessionRegistry(TestEmitter::new);
    var controller = new NotificationApiController(registry);

    var emitter = controller.connect(10L);

    assertNotNull(emitter);
    assertTrue(registry.find(10L).isPresent());
  }

  private static class TestEmitter extends SseEmitter {

  }
}
