package io.avaje.inject.events;

import io.avaje.inject.event.Event;
import io.avaje.inject.event.ObserverManager;

public class TestEvent extends Event<String> {

  public TestEvent(ObserverManager manager) {
    super(manager, String.class);
  }
}
