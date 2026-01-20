package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a bean that has prototype scope.
 *
 * <p>A new instance of this bean will be created each time it is requested or wired.
 *
 * <pre>{@code
 * @Prototype
 * class EmailSendHandler {
 *
 *   ...
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Prototype {

  /**
   * Enable @{@link PreDestroy} methods. When enabled, the container will hold a reference to this
   * bean in order to call any @PreDestroy annotated methods when the container is shutdown. Be
   * aware that continuously creating prototype beans without closing the scope may lead to memory
   * leaks when this flag is enabled.
   */
  boolean enablePreDestroy() default false;
}
