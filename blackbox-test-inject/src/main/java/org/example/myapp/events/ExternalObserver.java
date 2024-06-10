package org.example.myapp.events;

import org.other.one.SomeOptionalDep;

import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class ExternalObserver {

  boolean invoked;
  SomeOptionalDep recievedEvent;

  public void observe(@Observes SomeOptionalDep event) {
    invoked = true;

    this.recievedEvent = event;
  }
}
