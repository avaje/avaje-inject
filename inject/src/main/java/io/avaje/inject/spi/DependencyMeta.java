package io.avaje.inject.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hold bean dependency metadata intended for internal use by code generation (Java annotation processing).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
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
   * The aspect this component provides.
   */
  String providesAspect() default "";

  /**
   * The interfaces the bean implements.
   */
  String[] provides() default {};

  /**
   * The list of dependencies this bean requires.
   */
  String[] dependsOn() default {};

  /**
   * Type deemed to be reasonable to provide to external module.
   * <p>
   * Used to support multiple module wiring automatically (as alternative to using explicit InjectModule annotation).
   */
  String autoProvides() default "";

}
