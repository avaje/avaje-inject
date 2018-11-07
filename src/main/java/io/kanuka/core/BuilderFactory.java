package io.kanuka.core;

public class BuilderFactory {

  /**
   * Create the root level Builder.
   */
  public static Builder newRootBuilder() {
    return new DBuilder(null);
  }

  /**
   * Create a Builder for the named context (module).
   */
  public static Builder newBuilder(String name) {
    return new DBuilder(name);
  }
}
