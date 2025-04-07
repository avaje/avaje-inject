package org.example.myapp.duplicate;

import jakarta.inject.Singleton;

@Singleton
public class SameType {
  @Singleton
  public static class Inner {}
}
