package io.avaje.inject;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.avaje.inject.events.ObserverManager;
import io.avaje.inject.events.TestEvent;
import io.avaje.inject.events.TestGenericEvent;
import io.avaje.inject.spi.GenericType;

class DObserverManagerTest {

  ObserverManager manager = new DObserverManager();

  @Test
  void test() {
    AtomicBoolean aBoolean = new AtomicBoolean();
    final var testEvent = new TestEvent(manager);
    testEvent.fire("sus");

    assertThat(aBoolean.get()).isFalse();
    manager.<String>registerObserver(false, String.class, s -> aBoolean.set(true), null);

    testEvent.fire("sus");
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testRegisterAsync() throws InterruptedException {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<List<String>>registerObserver(
        true, new GenericType<List<String>>() {}.type(), s -> aBoolean.set(true), null);

    new TestGenericEvent(manager).fire(List.of("str"));
    Thread.sleep(200);
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testAsync() throws InterruptedException {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<List<String>>registerObserver(
        false, new GenericType<List<String>>() {}.type(), s -> aBoolean.set(true), null);

    new TestGenericEvent(manager).fireAsync(List.of("str"));
    Thread.sleep(200);
    assertThat(aBoolean.get()).isTrue();
  }
}
