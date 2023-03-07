package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the bean should only be wired when the specified beans are already contained in
 * the {@link BeanScope}.
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;Requires(value=OtherService.class)
 *     &#064;Bean
 *     public MyService myService() {
 *         ...
 *     }
 *
 * }</pre>
 *
 * <p>In the sample above the condition will match if a bean of type {@code OtherService} is already
 * registered in the {@link BeanScope}.
 *
 * <p>The condition can only match the bean definitions that have been registered by the scope so
 * far and, as such, it is strongly recommended to use this condition on auto-configuration classes
 * only. If a candidate bean may be created by another module, make sure that the one using this
 * condition runs after.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Requires {

  /**
   * The class types of beans that should be registered. The condition matches when all beans of each
   * class specified is contained in the {@link BeanScope}.
   *
   * @return the class types of beans to check
   */
  Class<?>[] value() default {};

  /**
   * The class type names of beans that should be registered. The condition matches when beans of all
   * classes specified are contained in the {@link BeanScope}.
   *
   * @return the class type names of beans to check
   */
  String[] type() default {};

  /**
   * The qualifiers that should be registered. The condition matches when all qualifier specified are registered in the {@link BeanScope}.
   *
   * @return the names of beans to check
   */
  String[] qualifers() default {};
}
