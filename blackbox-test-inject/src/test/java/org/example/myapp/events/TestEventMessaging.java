package org.example.myapp.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.other.one.SomeOptionalDep;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class TestEventMessaging {

  @Inject ExternalObserver observer;
  @Inject ExternalEventPublisher event;

  @BeforeEach
  void before() {
    observer.invoked = false;
    observer.recievedEvent = null;
  }

  @Test
  void test() {
    var message = new SomeOptionalDep() {};

    event.fire(message);

    assertThat(observer.invoked).isTrue();
    assertThat(observer.recievedEvent).isSameAs(message);
  }
}
