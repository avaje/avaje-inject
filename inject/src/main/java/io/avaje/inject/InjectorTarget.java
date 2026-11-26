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
 *
 * <h3>Subclasses</h3>
 *
 * <p>When the instance passed to {@link BeanScope#inject(Object)} is a subclass of the annotated
 * type, the injector of the nearest annotated supertype is used. {@code @Inject} members declared
 * only on an unannotated subclass are not injected; annotate the subclass with
 * {@code @InjectorTarget} as well to include them.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface InjectorTarget {

  /**
   * Enable {@link PreDestroy} methods and {@link AutoCloseable#close()} for injected instances.
   *
   * <p>When enabled, the BeanScope holds a reference to each injected instance in order to invoke
   * the destroy callback when the BeanScope is closed. Be aware that injecting many short-lived
   * instances into a long-lived BeanScope with this enabled will retain those instances, and may
   * lead to memory leaks.
   */
  boolean enablePreDestroy() default false;

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

    /**
     * Enable {@link PreDestroy} methods and {@link AutoCloseable#close()} for the imported types.
     *
     * @see InjectorTarget#enablePreDestroy()
     */
    boolean enablePreDestroy() default false;
  }
}
