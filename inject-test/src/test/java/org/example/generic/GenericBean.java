package org.example.generic;

import java.util.function.Supplier;

import io.avaje.inject.test.TestScope;

@TestScope
public class GenericBean implements Supplier<Short> {

  @Override
  public Short get() {
    return null;
  }
}
