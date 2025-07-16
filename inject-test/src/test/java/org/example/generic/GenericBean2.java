package org.example.generic;

import java.util.function.Supplier;

import jakarta.inject.Singleton;

@Singleton
public class GenericBean2 implements Supplier<Long> {

  @Override
  public Long get() {
    return null;
  }
}
