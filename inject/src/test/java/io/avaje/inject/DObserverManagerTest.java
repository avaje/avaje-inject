package io.avaje.inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.avaje.inject.event.Observer;
import io.avaje.inject.event.ObserverManager;
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
  void testAsync() throws InterruptedException, ExecutionException {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<String>registerObserver(
        String.class, new Observer<>(0, true, s -> aBoolean.set(true), ""));

    new TestEvent(manager).fireAsync("str").toCompletableFuture().get();
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testGenericAsync() throws InterruptedException, ExecutionException {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<List<String>>registerObserver(
        new GenericType<List<String>>() {}.type(),
        new Observer<>(0, true, s -> aBoolean.set(true), ""));

    new TestGenericEvent(manager).fireAsync(List.of("str")).toCompletableFuture().get();
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testError() throws InterruptedException {
    final var testEvent = new TestEvent(manager);
    testEvent.fire("sus");

    manager.<String>registerObserver(
        String.class, new Observer<>(0, false, s->{throw new IllegalArgumentException();}, ""));

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> testEvent.fire("sus"));
  }

  @Test
  void testAsyncError() throws InterruptedException, ExecutionException {
    final var testEvent = new TestEvent(manager);

    manager.<String>registerObserver(
        String.class, new Observer<>(0, true,  s->{throw new IllegalArgumentException();}, ""));

    testEvent.fire("sus");
    var future = testEvent.fireAsync("sus").toCompletableFuture();
    assertThatExceptionOfType(CompletionException.class)
        .isThrownBy(() -> future.join())
        .satisfies(
            e -> {
              assert e.getSuppressed()[0] instanceof IllegalArgumentException;
            });
  }
}
