package org.example.myapp.duplicate.two;

import jakarta.inject.Singleton;

@Singleton
public class SameType {
  @Singleton
  public static class Inner {}
}
