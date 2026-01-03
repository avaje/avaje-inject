package org.example.myapp.events;

import org.other.one.SomeOptionalDep;

import io.avaje.inject.Lazy;
import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Lazy
@Singleton
public class LazyObserver {

 private SomeOptionalDep recievedEvent;

  public void observe(@Observes SomeOptionalDep event) {

    this.recievedEvent = event;
  }

  public SomeOptionalDep recievedEvent() {
	return recievedEvent;
  }
}
