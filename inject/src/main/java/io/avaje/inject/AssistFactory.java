package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a bean which we want avaje-inject to generate the factory for.
 * <p>
 * The bean will have some properties that are normal dependency injection
 * components that will be injected and others that will be parameters
 * to the method on the factory - these parameters are annotated with
 * {@code @Assisted}.
 *
 * <h3>Example</h3>
 * <p>
 * Have an interface which we want avaje-inject to generate the implementation.
 * We desire this because the factory has some dependencies that are components
 * that avaje-inject should inject for us and some other dependencies that are
 * provided by the application (and annotated with {@code Assisted}).
 * <pre>{@code
 *
 * public interface CssFactory {
 *   Scanner scanner(Path file);
 * }
 *
 * }</pre>
 * <p>
 * Have a bean annotated with {@code @AssistFactory} that specifies the factory.
 * Any dependencies that are annotated with {@code Assist} will be parameters
 * of the factory method, the other dependencies are normal components that
 * will be injected.
 *
 * <pre>{@code
 *
 * @AssistFactory(CssFactory.class)
 * class CssScanner implements Scanner {
 *
 *   private final Path path;
 *   private final SomeComponent someComponent;
 *
 *   CssScanner(@Assisted Path path, SomeComponent someComponent) {
 *     this.path = path;
 *     this.someComponent = someComponent;
 *   }
 *
 *   ...
 * }
 *
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssistFactory {

  /**
   * Specify the factory interface for which the implementation will be generated.
   */
  Class<?> value();
}
