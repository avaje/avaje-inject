package org.example.observes;

import io.avaje.inject.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class Observer {
  boolean invoked = false;

  void observe(@Observes CustomEvent e) {
    invoked = true;
  }
}
