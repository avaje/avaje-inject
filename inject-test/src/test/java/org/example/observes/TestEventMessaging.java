package org.example.observes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.avaje.inject.event.Event;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class TestEventMessaging {

  @Inject MyObserver observer;
  @Inject MyQualifiedObserver qualifiedObserver;
  @Inject MyObserverInjected observerInjected;
  @Inject Event<CustomEvent> event;

  @BeforeEach
  void before() {
    observer.invoked = false;
    observer.event = null;
    qualifiedObserver.invoked= false;
    qualifiedObserver.event = null;
    observerInjected.invoked= false;
    observerInjected.event = null;
  }

  @Test
  void test() {
    var message = new CustomEvent("hi");

    event.fire(message);

    assertThat(observer.invoked).isTrue();
    assertThat(observer.event).isSameAs(message);
    assertThat(qualifiedObserver.invoked).isFalse();
    assertThat(observerInjected.invoked).isTrue();
    assertThat(observerInjected.event).isSameAs(message);
  }

  @Test
  void testWithQualified() {
    var message = new CustomEvent("hi");

    event.fire(message, "qual");

    assertThat(observer.invoked).isFalse();
    assertThat(observer.event).isNull();
    assertThat(qualifiedObserver.invoked).isTrue();
    assertThat(qualifiedObserver.event).isSameAs(message);
    assertThat(observerInjected.invoked).isFalse();
    assertThat(observerInjected.event).isNull();
  }
}
