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
 * Have an interface which we want avaje-inject to generate an implementation.
 * We desire this because the factory has some dependencies that are components
 * that avaje-inject should inject for us and some other dependencies that are
 * provided by the application (and annotated with {@code Assisted}).
 * <p>
 * The factory interface must only have 1 method defined.
 *
 * <pre>{@code
 *
 * public interface CssFactory {
 *
 *   Scanner scanner(Path myPath);
 * }
 *
 * }</pre>
 * <p>
 * Have a bean annotated with {@code @AssistFactory} that specifies the factory.
 * Any dependencies that are annotated with {@code Assisted} will be parameters
 * of the factory method, the other dependencies are normal components that
 * will be injected.
 * <p>
 * The {@code Assisted} parameters must match the factory method parameters
 * by name and type. In this example {@code Path myPath} match in both CssScanner
 * and in CssFactory.
 *
 * <pre>{@code
 *
 * @AssistFactory(CssFactory.class)
 * class CssScanner implements Scanner {
 *
 *   private final Path myPath;
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
