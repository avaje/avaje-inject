package io.avaje.inject.spi;

/**
 * Hold bean dependency meta data intended for internal use by code generation (Java annotation processing).
 */
public @interface DependencyMeta {

  /**
   * The bean type.
   */
  String type();

  /**
   * The qualified name of the dependency being provided.
   */
  String name() default "";

  /**
   * The bean factory method (for <code>@Bean</code> annotated methods).
   */
  String method() default "";

  /**
   * The interfaces the bean implements.
   */
  String[] provides() default {};

  /**
   * The list of dependencies.
   */
  String[] dependsOn() default {};

}
