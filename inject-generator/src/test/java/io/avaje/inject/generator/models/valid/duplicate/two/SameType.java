package io.avaje.inject.generator.models.valid.duplicate.two;

import jakarta.inject.Singleton;

@Singleton
public class SameType {
  @Singleton
  static class Inner {}
}
