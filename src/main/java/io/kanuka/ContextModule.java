package io.kanuka;

public @interface ContextModule {

  /**
   * The name of the context.
   */
  String name() default "";

  /**
   * The list of dependencies this context depends on.
   */
  String[] dependsOn() default {};

}
