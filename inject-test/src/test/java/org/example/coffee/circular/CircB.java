package org.example.coffee.circular;

import javax.inject.Singleton;

@Singleton
class CircB {

  final CircC circC;

  CircB(CircC circC) {
    this.circC = circC;
  }

  @Override
  public String toString() {
    return "CircB-" + circC;
  }
}
