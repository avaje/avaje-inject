package org.example.observes;

public class CustomEvent {
  private final String string;

  public CustomEvent(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }
}
