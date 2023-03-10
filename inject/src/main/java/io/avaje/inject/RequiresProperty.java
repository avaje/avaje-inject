package io.avaje.inject;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Expresses a requirement for a bean to be wired/registered into the {@link BeanScope}.
 *
 * <pre>{@code
 *
 *   @Configuration
 *   public class MyAutoConfiguration {
 *
 *     @Bean
 *     @RequiresProperty("use.service")
 *     public MyService myService() {
 *         ...
 *     }
 *
 *   }
 *
 * }</pre>
 *
 * <p>
 * In the sample above the MyService bean will get wired only if <code>use.service</code>
 * is set in Java system properties / Avaje Config.
 * <p>
 * {@link io.avaje.inject.spi.PropertyRequiresPlugin} is used to test the property
 * conditions and is loaded via {@link java.util.ServiceLoader}.
 * <p>
 * Avaje Config provides an implementation and if it is included in the classpath then
 * Avaje Config will be used to test the property conditions.
 * <p>
 * If no PropertyRequiresPlugin is found then the default implementation is used which uses
 * {@link System#getProperty(String)} and {@link System#getenv(String)}.
 */
@Retention(RUNTIME)
@Repeatable(RequiresProperty.Container.class)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface RequiresProperty {

  /**
   * Expresses that the given property should be set for the bean to load.
   *
   * @return the property to check
   */
  String value() default "";

  /**
   * Expresses that the bean or configuration will only be registered if the given properties are
   * missing.
   *
   * @return the properties to check
   */
  String[] missingProperties() default {};

  /**
   * Used in combination with value() to express the required value of the property.
   *
   * @return the value the property should be
   */
  String equalTo() default "";

  /**
   * Constraint a property to not equal the given value.
   *
   * @return the value the property should not be
   */
  String notEqualTo() default "";

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Container {

    /** @return The required dependencies */
    RequiresProperty[] value();
  }
}
