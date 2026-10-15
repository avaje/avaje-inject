package org.example.myapp.other;

import io.avaje.inject.Component;
import io.avaje.inject.Lazy;

public class Nested {

  @Component
  @Lazy(Lazy.Kind.PROVIDER)
  static class Leaf2
  {
  }
}
