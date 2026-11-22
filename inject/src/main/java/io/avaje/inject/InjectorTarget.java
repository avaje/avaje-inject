package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generate an injector for a type that avaje-inject does not construct itself.
 *
 * <p>Place this on the externally created type and annotate the fields or setter methods that need
 * injecting with {@code @Inject} as normal. The generated injector is registered with the module
 * and can be used via {@link BeanScope#inject(Object)}.
 *
 * <h3>Example</h3>
 *
 * <pre>{@code
 * @InjectorTarget
 * public class MyFrameworkServlet {
 *
 *   private Dep1 dep1;
 *
 *   @Inject
 *   void setDep1(Dep1 dep1) {
 *     this.dep1 = dep1;
 *   }
 * }
 * }</pre>
 *
 * <pre>{@code
 * MyFrameworkServlet servlet = ExternalFramework.create();
 * scope.inject(servlet);
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface InjectorTarget {

  /**
   * Generate injectors for types that cannot be annotated with {@code @InjectorTarget}.
   *
   * <p>Place this on a type, package, or module. Imported target classes and their injectable
   * members must be public.
   */
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
  @interface Import {

    /** Types to generate injectors for. */
    Class<?>[] value();
  }
}
