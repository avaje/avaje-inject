package io.avaje.inject.generator.models.valid.pkg_private;

import jakarta.inject.Singleton;

@Singleton
public class Calculator {
  private final Adder adder;

  public Calculator(Adder adder) {
    this.adder = adder;
  }

  public int sum(int a, int b) {
    return adder.add(a, b);
  }
}