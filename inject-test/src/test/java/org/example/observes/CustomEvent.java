package org.example.observes;

import io.avaje.inject.event.EventType;

@EventType
public class CustomEvent {
  private final String string;

  public CustomEvent(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }
}
