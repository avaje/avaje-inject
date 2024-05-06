package org.example.observes;

import io.avaje.inject.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class MyStrQualifiedObserver {

  boolean invoked = false;
  CustomEvent event;

  void observe(@Observes @StrQualifier("foo") CustomEvent e) {
    invoked = true;
    event = e;
  }

  void clear() {
    invoked = false;
    event = null;
  }
}
