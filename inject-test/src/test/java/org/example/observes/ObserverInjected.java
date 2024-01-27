package org.example.observes;

import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class ObserverInjected {

  private boolean invoked = false;

  void observe(@Observes CustomEvent e, Observer observer) {
    invoked = true;
  }

  public boolean wasInvoked() {
    return invoked;
  }
}
