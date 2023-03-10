package io.avaje.inject;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Expresses a requirement for a bean to be wired/registered into the {@link BeanScope}.
 *
 * <pre class="code">{@code
 *
 *   @Configuration
 *   public class MyAutoConfiguration {
 *
 *     @Bean
 *     @Requires(beans = OtherService.class)
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
@Repeatable(RequiresBean.RequireBeans.class)
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
  Class<?>[] missingBeans() default {};

  /**
   * Expresses that a {@link @Named} or {@link @Qualifier} annotation marker of the given name should be
   * available in the {@link BeanScope}.
   *
   * @return the names of beans to check
   */
  String[] qualifiers() default {};
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface RequireBeans {

    /** @return The required dependencies */
    RequiresBean[] value();
  }
}
