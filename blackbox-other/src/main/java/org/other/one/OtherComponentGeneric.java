package org.other.one;

import java.util.function.Supplier;

import io.avaje.inject.Component;

@Component
public class OtherComponentGeneric implements Supplier<String> {

  @Override
  public String get() {
    return "hello";
  }
}
