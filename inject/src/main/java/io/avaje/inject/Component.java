package io.avaje.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identify a bean as component with singleton scope that avaje-inject will use.
 * <p>
 * This is an alternative to using the standard <code>@Singleton</code> annotation.
 * We generally use <code>@Component</code> when we:
 * <ul>
 *   <li>Want to use avaje-inject in a project that has some other library using <code>@Singleton</code></li>
 *   <li>Want to support BOTH <code>javax.inject</code> and <code>jakarta.inject</code></li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>{@code
 *
 * @Component
 * class MyEmailSender implements EmailSender {
 *
 *   ...
 * }
 * }</pre>
 *
 * <h3>Ignoring <em>@Singleton</em></h3>
 * <p>
 * Set {@link InjectModule#ignoreSingleton()} <code>true</code> to get avaje-inject to ignore
 * classes annotated with <code>@Singleton</code>. Typically, we want another DI library to use
 * those classes and want avaje-inject to co-exist independently.
 * <p>
 *
 * <pre>{@code
 *
 *   @InjectModule(name = "coffee", ignoreSingleton = true)
 *   package coffee;
 *
 *   import io.avaje.inject.InjectModule;
 *
 * }</pre>
 *
 * @see InjectModule#ignoreSingleton()
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Component {

  /**
   * Specify types to generate DI classes for.
   *
   * <p>These types are typically in an external project / dependency or otherwise types that we
   * can't or don't want to explicitly annotate with {@code @Singleton}/{@code @Component}.
   *
   * <p>Typically, we put this annotation on a package/module-info.
   *
   * <pre>{@code
   * Component.Import({CustomerService.class, ProductService.class, ...})
   * package org.example.processor;
   *
   * }</pre>
   */
  @Retention(CLASS)
  @Target({TYPE, PACKAGE, MODULE})
  @interface Import {

    /**
     * Types to generate DI classes for.
     */
    Class<?>[] value();
  }
}
