package io.avaje.inject.events.events;

import io.avaje.inject.events.Event;
import io.avaje.inject.events.ObserverManager;

public class TestEvent extends Event<String> {

  public TestEvent(ObserverManager manager) {
    super(manager, String.class);
  }
}
