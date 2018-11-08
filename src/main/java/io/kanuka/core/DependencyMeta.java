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
   * The interfaces the bean implements.
   */
  String[] provides() default {};

  /**
   * The list of dependencies.
   */
  String[] dependsOn() default {};

}
