package org.yechan.remittance.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationSessionRegistryTest {

  @Test
  void connectRegisterStoresEmitter() {
    var registry = new NotificationSessionRegistry(TestEmitter::new);

    var emitter = registry.connectRegister(1L);

    assertNotNull(emitter);
    assertTrue(registry.find(1L).isPresent());
  }

  @Test
  void pushSendsWhenSessionExists() {
    var registry = new NotificationSessionRegistry(TestEmitter::new);
    var emitter = (TestEmitter) registry.connectRegister(2L);

    var sent = registry.push(2L, new TestPayload("TRANSFER_RECEIVED"));

    assertTrue(sent);
    assertEquals(1, emitter.sendCount());
    assertEquals("TRANSFER_RECEIVED", emitter.lastPayload().type());
  }

  @Test
  void pushReturnsFalseWhenMissing() {
    var registry = new NotificationSessionRegistry(TestEmitter::new);

    var sent = registry.push(999L, new TestPayload("TRANSFER_RECEIVED"));

    assertFalse(sent);
  }

  @Test
  void completeRemovesSession() {
    var registry = new NotificationSessionRegistry(TestEmitter::new);
    var emitter = registry.connectRegister(3L);

    emitter.complete();

    assertTrue(registry.find(3L).isEmpty());
  }

  private record TestPayload(String type) {

  }

  private static class TestEmitter extends SseEmitter {

    private final AtomicReference<Object> lastPayload = new AtomicReference<>();
    private int sendCount;
    private Runnable completionCallback;
    private Consumer<Throwable> errorCallback;

    @Override
    public void onCompletion(@NonNull Runnable callback) {
      completionCallback = callback;
      super.onCompletion(callback);
    }

    @Override
    public void onTimeout(@NonNull Runnable callback) {
      super.onTimeout(callback);
    }

    @Override
    public void onError(@NonNull Consumer<Throwable> callback) {
      errorCallback = callback;
      super.onError(callback);
    }

    @Override
    public void complete() {
      super.complete();
      if (completionCallback != null) {
        completionCallback.run();
      }
    }

    @Override
    public void completeWithError(@NonNull Throwable ex) {
      super.completeWithError(ex);
      if (errorCallback != null) {
        errorCallback.accept(ex);
      }
    }

    @Override
    public void send(@NonNull Object object) {
      lastPayload.set(object);
      sendCount++;
    }

    int sendCount() {
      return sendCount;
    }

    TestPayload lastPayload() {
      return Optional.ofNullable(lastPayload.get())
          .map(TestPayload.class::cast)
          .orElse(null);
    }
  }
}
