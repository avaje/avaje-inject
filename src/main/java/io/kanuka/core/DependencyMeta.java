package io.kanuka.core;

/**
 * Hold bean dependency meta data intended for internal use by code generation (Java annotation processing).
 */
public @interface DependencyMeta {

  /**
   * The bean type.
   */
  String type();

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
