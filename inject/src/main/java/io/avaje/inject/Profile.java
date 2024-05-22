package io.avaje.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Expresses a requirement for a bean to be wired/registered into the {@link BeanScope}.
 *
 * <pre>{@code
 * @Factory
 * public class MyAutoConfiguration {
 *
 *   @Bean
 *   @Profile("test")
 *   public MyService myService() {
 *       ...
 *   }
 *
 * }
 *
 * }</pre>
 *
 * <p>In the sample above, the MyService bean will get wired only if <code>avaje.profiles</code> is
 * set to "test" in the {@link io.avaje.inject.spi.PropertyRequiresPlugin}.
 *
 * <p>Avaje Config provides an implementation and if it is included in the classpath then Avaje
 * Config will be used to test the property conditions.
 *
 * <p>If no PropertyRequiresPlugin is found then the default implementation is used which uses
 * {@link System#getProperty(String)} and {@link System#getenv(String)}.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface Profile {

  /**
   * Expresses that any of the given profiles must be set for the bean to load.
   *
   * @return the property to check
   */
  String[] value() default {};

  /**
   * Expresses that all of the given profiles must be set for the bean to load.
   *
   * @return the property to check
   */
  String[] all() default {};

  /**
   * Expresses that none of the given profiles must be set for the bean to load.
   *
   * @return the properties to check
   */
  String[] none() default {};
}
