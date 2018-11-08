package io.kanuka.core;

/**
 * Factory for creating Builder instances.
 *
 * These Builders are typically used by generated code (Java annotation processing - kanuka-generator).
 */
public class BuilderFactory {

  /**
   * Create the root level Builder.
   */
  public static Builder newRootBuilder() {
    return new DBuilder(null, null);
  }

  /**
   * Create a Builder for the named context (module).
   *
   * @param name the name of the module / bean context
   * @param dependsOn the names of modules this module is depends on.
   */
  public static Builder newBuilder(String name, String... dependsOn) {
    return new DBuilder(name, dependsOn);
  }
}
