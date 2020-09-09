package org.example.coffee.circular;

import javax.inject.Singleton;

@Singleton
public class CircB {

  private final CircC circC;

  public CircB(CircC circC) {
    this.circC = circC;
  }
}
