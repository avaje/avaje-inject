package io.avaje.inject;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.avaje.inject.events.Observer;
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
    manager.<String>registerObserver(
        String.class, new Observer<>(0, false, s -> aBoolean.set(true), ""));

    testEvent.fire("sus");
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testAsync() throws InterruptedException {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<String>registerObserver(
        String.class, new Observer<>(0, true, s -> aBoolean.set(true), ""));

    new TestEvent(manager).fireAsync("str");
    Thread.sleep(500);
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testGenericAsync() throws InterruptedException {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<List<String>>registerObserver(
        new GenericType<List<String>>() {}.type(),
        new Observer<>(0, false, s -> aBoolean.set(true), ""));

    new TestGenericEvent(manager).fire(List.of("str"));
    Thread.sleep(200);
    assertThat(aBoolean.get()).isTrue();
  }
}
