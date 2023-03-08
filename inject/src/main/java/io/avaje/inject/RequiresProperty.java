package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Expresses a requirement for a bean to be wired/registered into the {@link BeanScope}.
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;Bean
 *     &#064;Requires(beans = OtherService.class)
 *     public MyService myService() {
 *         ...
 *     }
 *
 * }</pre>
 *
 * <p>In the sample above the MyService bean will get wired only if a bean of type {@code
 * OtherService} is already registered in the {@link BeanScope}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RequireConfigs.class)
public @interface RequiresProperty {

  /**
   * Expresses that beans of the given types should be available in the {@link BeanScope}.
   *
   * @return the class types of beans to check
   */
  String value() default "";

  /**
   * Expresses that the bean or configuration will only be registered if the given properties is
   * missing.
   *
   * @return the properties to check
   */
  String[] missingProperties() default {};

  /**
   * Expresses that a {@link @Named} or {@link @Qualifier} marker of the given name should be
   * available in the {@link BeanScope}.
   *
   * @return the names of beans to check
   */
  String equalTo() default "";

  /**
   * Expresses that beans of the given types should not be available in the {@link BeanScope}.
   *
   * @return the class types of beans to check
   */
  String notEqualTo() default "";
}
