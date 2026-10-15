package org.example.myapp;

import io.avaje.inject.Component;
import io.avaje.inject.Lazy;

public class NestedComponent {

  @Component
  @Lazy(Lazy.Kind.PROVIDER)
  static class Inner {
  }
}
