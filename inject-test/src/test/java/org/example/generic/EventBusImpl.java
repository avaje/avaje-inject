package org.example.generic;

import jakarta.inject.Singleton;

@Singleton
public class EventBusImpl implements EventBus<Object, Subscriber<?>> {

  public String wheels() {
    return "the wheels on the bus go round and round";
  }
}
