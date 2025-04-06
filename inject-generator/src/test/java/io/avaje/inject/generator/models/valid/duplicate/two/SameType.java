package io.avaje.inject.generator.models.valid.duplicate.two;

import jakarta.inject.Singleton;

@Singleton
public class SameType {
  @Singleton
  public static class Inner {}
}
