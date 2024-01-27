package org.example.observes;

import io.avaje.inject.events.Observes;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class QualifiedObserver {

  private boolean invoked = false;

  void observe(@Observes @Named("qual") CustomEvent e) {
    invoked = true;
  }

  public boolean wasInvoked() {
    return invoked;
  }
}
