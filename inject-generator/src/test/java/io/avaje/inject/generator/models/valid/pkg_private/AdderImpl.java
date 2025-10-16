package io.avaje.inject.generator.models.valid.pkg_private;

import jakarta.inject.Singleton;

@Singleton
class AdderImpl implements Adder {

  @Override
  public int add(int a, int b) {
    return a + b;
  }
}
