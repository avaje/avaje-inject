package io.avaje.inject;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Expresses a requirement for a bean to be wired/registered into the {@link BeanScope}.
 *
 * <pre>{@code
 *
 *   @Factory
 *   public class MyAutoConfiguration {
 *
 *     @Bean
 *     @RequiresBean(OtherService.class)
 *     public MyService myService() {
 *         ...
 *     }
 *   }
 *
 * }</pre>
 *
 * <p>In the sample above the MyService bean will get wired only if a bean of type {@code
 * OtherService} is already registered in the {@link BeanScope}.
 */
@Retention(RUNTIME)
@Repeatable(RequiresBean.Container.class)
@Target({TYPE, METHOD, ANNOTATION_TYPE})
public @interface RequiresBean {

  /**
   * Expresses that beans of the given types should be available in the {@link BeanScope}.
   *
   * @return the class types of beans to check
   */
  Class<?>[] value() default {};

  /**
   * Expresses that beans of the given types should not be available in the {@link BeanScope}.
   *
   * @return the class types of beans to check
   */
  Class<?>[] missing() default {};

  /**
   * Expresses that a {@link @Named} or {@link @Qualifier} annotation marker of the given name should be
   * available in the {@link BeanScope}.
   *
   * @return the names of beans to check
   */
  String[] qualifiers() default {};

  @Retention(RUNTIME)
  @Target({TYPE, METHOD, ANNOTATION_TYPE})
  @interface Container {

    /**
     * The required dependencies.
     */
    RequiresBean[] value();
  }
}
