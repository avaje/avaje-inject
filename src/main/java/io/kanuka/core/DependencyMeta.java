package io.kanuka.core;

public @interface DependencyMeta {

  /**
   * The bean type.
   */
  String type();

  /**
   * The interfaces and class annotations the bean has (to register into lists).
   */
  String[] provides() default {};

  /**
   * The list of dependencies with optional and named.
   */
  String[] dependsOn() default {};

//  /**
//   * The name of the bean (as defined by an <code>@Named</code> annotation).
//   */
//  String name() default "";
//
//  /**
//   * constructor, method, fields.
//   */
//  String injectType() default "";

//  /**
//   * Set to true if a $k class is expected to create an instance due to restricted visibility.
//   */
//  boolean factory() default false;
//
//  /**
//   * Set to true of a $k class is expected to create a lifecycle wrapper.
//   */
//  boolean lifecycle() default false;

}
