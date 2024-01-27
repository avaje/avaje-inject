package org.example.observes;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import io.avaje.inject.events.Event;
import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
public class TestEventMessaging {

  @Inject Observer observer;
  @Inject QualifiedObserver qualifiedObserver;
  @Inject ObserverInjected observerInjected;
  @Inject Event<CustomEvent> event;

  @Test
  void test() {
    var message = new CustomEvent("hi");

    event.fire(message);

    assertThat(observer.wasInvoked()).isTrue();
    assertThat(qualifiedObserver.wasInvoked()).isFalse();
    assertThat(observerInjected.wasInvoked()).isTrue();
  }

  @Test
  void testWithQualified() {
    var message = new CustomEvent("hi");

    event.fire(message, "qual");

    assertThat(observer.wasInvoked()).isTrue();
    assertThat(qualifiedObserver.wasInvoked()).isTrue();
    assertThat(observerInjected.wasInvoked()).isTrue();
  }
}
