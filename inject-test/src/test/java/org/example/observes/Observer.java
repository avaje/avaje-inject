package org.example.observes;

import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class Observer {
  private boolean invoked = false;

  void observe(@Observes CustomEvent e) {
    invoked = true;
  }

  public boolean wasInvoked() {
    return invoked;
  }
}
