package org.example.observes;

import io.avaje.inject.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class MyObserver {
  boolean invoked = false;
  CustomEvent event;

  void observe(@Observes CustomEvent e) {
    invoked = true;
    event = e;
  }
}
