package io.avaje.inject.events;

import java.util.List;

import io.avaje.inject.spi.GenericType;

public class TestGenericEvent extends Event<List<String>> {

  public TestGenericEvent(ObserverManager manager) {
    super(manager.observers((new GenericType<List<String>>() {}.type())));
  }
}
