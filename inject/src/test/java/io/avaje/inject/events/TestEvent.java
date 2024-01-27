package io.avaje.inject.events;

public class TestEvent extends Event<String> {

  public TestEvent(ObserverManager manager) {
    super(manager.observersByType(String.class));
  }
}
