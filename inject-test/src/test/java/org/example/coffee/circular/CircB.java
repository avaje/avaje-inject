package org.example.coffee.circular;

import jakarta.inject.Singleton;

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
