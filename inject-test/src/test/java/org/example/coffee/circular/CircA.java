package org.example.coffee.circular;

import jakarta.inject.Singleton;

@Singleton
class CircA {

  final CircB circB;

  CircA(CircB circB) {
    this.circB = circB;
  }

  @Override
  public String toString() {
    return "CircA-" + circB;
  }
}
