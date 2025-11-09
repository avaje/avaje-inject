package io.avaje.inject.events.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.avaje.inject.events.Observer;
import io.avaje.inject.events.events.TestEvent;
import io.avaje.inject.events.events.TestGenericEvent;
import io.avaje.inject.spi.GenericType;

class DObserverManagerTest {

  DObserverManager manager = new DObserverManager();

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
  void testPriority() {
    var l = new ArrayList<String>();

    manager.<String>registerObserver(String.class, new Observer<>(0, false, s -> l.add("1"), ""));
    manager.<String>registerObserver(String.class, new Observer<>(5, false, s -> l.add("5"), ""));
    manager.<String>registerObserver(String.class, new Observer<>(2, false, s -> l.add("2"), ""));

    new TestEvent(manager).fire("str");
    assertThat(l).containsExactly("1", "2", "5");
  }

  @Test
  void testAsync() {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<String>registerObserver(
        String.class, new Observer<>(0, true, s -> aBoolean.set(true), ""));

    new TestEvent(manager).fireAsync("str").toCompletableFuture().join();
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testAsyncExecutor() {
    AtomicBoolean aBoolean = new AtomicBoolean();
    var exec = Executors.newSingleThreadScheduledExecutor();
    manager.post(exec);
    assertThat(exec).isSameAs(manager.asyncExecutor());
    manager.<String>registerObserver(
        String.class, new Observer<>(0, true, s -> aBoolean.set(true), ""));

    new TestEvent(manager).fireAsync("str").toCompletableFuture().join();
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testAsyncPriority() {
    var l = new ArrayList<String>();

    manager.<String>registerObserver(String.class, new Observer<>(0, true, s -> l.add("1"), ""));
    manager.<String>registerObserver(String.class, new Observer<>(5, true, s -> l.add("5"), ""));
    manager.<String>registerObserver(String.class, new Observer<>(2, true, s -> l.add("2"), ""));

    new TestEvent(manager).fireAsync("str").toCompletableFuture().join();
    assertThat(l).containsExactly("1", "2", "5");
  }

  @Test
  void testGenericAsync() {
    AtomicBoolean aBoolean = new AtomicBoolean();

    manager.<List<String>>registerObserver(
        new GenericType<List<String>>() {}.type(),
        new Observer<>(0, true, s -> aBoolean.set(true), ""));

    new TestGenericEvent(manager).fireAsync(List.of("str")).toCompletableFuture().join();
    assertThat(aBoolean.get()).isTrue();
  }

  @Test
  void testError() {
    final var testEvent = new TestEvent(manager);
    testEvent.fire("sus");

    manager.<String>registerObserver(
        String.class,
        new Observer<>(
            0,
            false,
            s -> {
              throw new IllegalArgumentException();
            },
            ""));

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> testEvent.fire("sus"));
  }

  @Test
  void testAsyncError() {
    final var testEvent = new TestEvent(manager);

    manager.<String>registerObserver(
        String.class,
        new Observer<>(
            0,
            true,
            s -> {
              throw new IllegalArgumentException();
            },
            ""));

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
