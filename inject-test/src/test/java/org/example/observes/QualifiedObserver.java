package org.example.observes;

import io.avaje.inject.event.Observes;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class QualifiedObserver {

  boolean invoked = false;

  void observe(@Observes @Named("qual") CustomEvent e) {
    invoked = true;
  }
}
