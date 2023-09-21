package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks methods on a <code>@Factory</code> bean that create dependencies.
 * <p>
 * See {@link Factory}.
 * </p>
 *
 * <pre>{@code
 *
 * @Factory
 * class Configuration {
 *
 *   private final StartConfig startConfig;
 *
 *   @Inject
 *   Configuration(StartConfig startConfig) {
 *     this.startConfig = startConfig;
 *   }
 *
 *   @Bean
 *   Foo buildFoo() {
 *     ...
 *     return new Foo(...);
 *   }
 *
 *   @Bean
 *   Bar buildBar(Foo foo, Bazz bazz) {
 *     ...
 *     return new Bar(...);
 *   }
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

  /**
   * Specify a method to be treated like a <code>@PostConstruct</code>
   */
  String initMethod() default "";

  /**
   * Specify a method to be treated like a <code>@PreDestroy</code>
   */
  String destroyMethod() default "";

  /**
   * Specify the priority of the destroy method to control its execution
   * order relative to other destroy methods.
   * <p>
   * Low values execute earlier than high values. All destroy methods without
   * any explicit priority are given a value of 1000.
   */
  int destroyPriority() default 0;

  /**
   * Specify that the concrete instance of the bean is an AutoCloseable. Use if your bean interface
   * doesn't extend AutoCloseable but the concrete class implements it.
   */
  boolean autoCloseable() default false;
}
