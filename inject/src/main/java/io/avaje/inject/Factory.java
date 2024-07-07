package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A singleton bean that has methods marked with the <code>@Bean</code> annotation.
 * <p>
 * Factory beans allow us to build beans using logic in methods. These methods for example
 * often use environment variables and system properties into account when building the bean.
 * </p>
 * <p>
 * Relative to <code>jakarta.inject.Provider</code>, Factory and Bean provides a more flexible
 * approach that allows dependencies on the method (as method parameters) as well as multiple
 * methods on the single factory bean.
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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Factory {
}
