package org.example.observes;

import io.avaje.inject.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class MyObserverInjected {

  boolean invoked = false;
  CustomEvent event;

  void observe(@Observes CustomEvent e, MyObserver observer) {
    invoked = true;
    event = e;
  }
}
