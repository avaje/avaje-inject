package org.example.observes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.avaje.inject.event.Event;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
public class TestEventMessaging {

  @Inject Observer observer;
  @Inject QualifiedObserver qualifiedObserver;
  @Inject ObserverInjected observerInjected;
  @Inject Event<CustomEvent> event;

  @BeforeEach
  void before() {
    observer.invoked = false;
    qualifiedObserver.invoked= false;
    observerInjected.invoked= false;
  }

  @Test
  void test() {
    var message = new CustomEvent("hi");

    event.fire(message);

    assertThat(observer.invoked).isTrue();
    assertThat(qualifiedObserver.invoked).isFalse();
    assertThat(observerInjected.invoked).isTrue();
  }

  @Test
  void testWithQualified() {
    var message = new CustomEvent("hi");

    event.fire(message, "qual");

    assertThat(observer.invoked).isFalse();
    assertThat(qualifiedObserver.invoked).isTrue();
    assertThat(observerInjected.invoked).isFalse();
  }
}
