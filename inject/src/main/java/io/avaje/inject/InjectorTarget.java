package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.avaje.inject.spi.Injector;

/**
 * Generate an {@link Injector} for a type that avaje-inject does not construct itself.
 *
 * <p>Place this on the externally created type and annotate the fields or setter methods that need
 * injecting with {@code @Inject} as normal. A generated bean is registered into the scope
 * implementing {@code Injector<Type>}; resolve it with {@link BeanScope#get(java.lang.reflect.Type)}.
 *
 * <h3>Example</h3>
 *
 * <pre>{@code
 * @InjectorTarget
 * public class LegacyServlet {
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
 * LegacyServlet servlet = ExternalFramework.create();
 * Injector<LegacyServlet> injector = scope.get(new GenericType<Injector<LegacyServlet>>() {});
 * injector.inject(servlet);
 * }</pre>
 *
 * @see Injector
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface InjectorTarget {}
