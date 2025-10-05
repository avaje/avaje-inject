package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or factory method bean to be initialized lazily.
 *
 * <p>When annotating a {@link Factory} class as {@code @Lazy}, the factory itself is not lazy but
 * all beans that it provides will have lazy initialization.
 *
 * <p>If the annotated class or factory method is an interface or has an additional no-args
 * constructor, a generated proxy bean will be wired for ultimate laziness.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
public @interface Lazy {

  /** Determine whether a compile-time proxy will be attempted. */
  boolean useProxy() default true;

  /**
   * Ensures that a compile-time proxy is generated, will fail compilation if missing conditions for
   * generation
   */
  boolean enforceProxy() default false;
}
