package org.example.myapp.externaldeps;

import io.avaje.inject.External;
import io.avaje.inject.RequiresProperty;
import io.avaje.inject.events.Event;
import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

/** {@code @External} on the non-event parameter of an observer method. */
@Singleton
@RequiresProperty(value = "externaldeps.observer", equalTo = "on")
public class ObserverExternal {

  public static class Ping {}

  private final Event<Ping> event;
  private ObserverExternalDep received;

  ObserverExternal(Event<Ping> event) {
    this.event = event;
  }

  public void fire() {
    event.fire(new Ping());
  }

  void onPing(@Observes Ping ping, @External ObserverExternalDep external) {
    this.received = external;
  }

  public ObserverExternalDep received() {
    return received;
  }
}
