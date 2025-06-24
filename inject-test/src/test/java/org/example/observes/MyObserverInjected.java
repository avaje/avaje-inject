package org.example.observes;

import io.avaje.inject.events.Event;
import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class MyObserverInjected {

  boolean invoked = false;
  CustomEvent event;

  void observe(
      @Observes CustomEvent e,
      MyObserver observer,
      @StrQualifier(value = "foo") Event<CustomEvent> publisher) {
    invoked = true;
    event = e;
  }
}
