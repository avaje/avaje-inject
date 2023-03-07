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
 *     &#064;Requires(beans=OtherService.class)
 *     &#064;Bean
 *     public MyService myService() {
 *         ...
 *     }
 *
 * }</pre>
 *
 * <p>In the sample above the MyService bean will get wired if a bean of type {@code OtherService}
 * is already registered in the {@link BeanScope}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = Requirements.class)
public @interface Requires {

  /**
   * Expresses that beans of the given types should be available in the {@link BeanScope}.
   *
   * @return the class types of beans to check
   */
  Class<?>[] beans() default {};

  /**
   * The qualifiers that should be registered. The condition matches when all qualifier specified
   * are registered in the {@link BeanScope}.
   *
   * @return the names of beans to check
   */
  String[] qualifiers() default {};
}
