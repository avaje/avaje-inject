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
  /** Determine the kind of lazy initialization. */
  Kind value() default Kind.AUTO_PROXY;

  /**
   * Control whether a compile-time proxy is generated to support lazy initialization.
   *
   * <p>When using {@link Kind#FORCE_PROXY} a compile-time error will occur if the conditions for
   * generating a proxy are not met (for example the class is final or has no no-args constructor).
   * When using {@link Kind#AUTO_PROXY} a warning will be issued and lazy initialization will fall
   * back to provider based lazy initialization.
   *
   * <p>When using {@link Kind#PROVIDER} no proxy is generated and lazy initialization is done via a
   * provider.
   */
  enum Kind {
    /**
     * Ensures that a compile-time proxy is generated, will fail compilation if missing conditions
     * for generation
     */
    FORCE_PROXY,
    /**
     * Attempt compile-time proxy, will warn and fallback to provider compilation if missing
     * conditions for generation
     */
    AUTO_PROXY,
    /** No proxy, use a provider based lazy initialization */
    PROVIDER
  }
}
