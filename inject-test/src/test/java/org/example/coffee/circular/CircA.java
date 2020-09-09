package org.example.coffee.circular;

import javax.inject.Singleton;

@Singleton
public class CircA {

  private final CircB circB;

  public CircA(CircB circB) {
    this.circB = circB;
  }
}
