package io.avaje.inject;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Expresses a requirement for a bean to be wired/registered into the {@link BeanScope}.
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;Bean
 *     &#064;RequiresProperty("use.service")
 *     public MyService myService() {
 *         ...
 *     }
 *
 * }</pre>
 *
 * <p>In the sample above the MyService bean will get wired only if use.service is set in Java
 * system properties / Avaje Config.
 */
@Retention(RUNTIME)
@Repeatable(RequireConfigs.class)
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
}
