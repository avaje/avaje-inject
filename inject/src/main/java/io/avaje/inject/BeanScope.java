package io.avaje.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * Holds beans created by dependency injection.
 * <p>
 * The beans have singleton scope, support lifecycle methods for postConstruct and
 * preDestroy and are created (wired) via dependency injection.
 * </p>
 *
 * <h3>Create a BeanScope</h3>
 * <p>
 * We can programmatically create a BeanScope via {@code BeanScope.builder()}.
 * </p>
 * <pre>{@code
 *
 *   // create a BeanScope ...
 *
 *   try (BeanScope scope = BeanScope.builder()
 *     .build()) {
 *
 *     CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
 *     coffeeMaker.makeIt()
 *   }
 *
 * }</pre>
 *
 * <h3>External dependencies</h3>
 * <p>
 * We can supporting external dependencies when creating the BeanScope.
 * Use the {@link io.avaje.inject.External @External} annotation.
 * <p>
 * For example, given we have Pump as an externally provided dependency.
 *
 * <pre>{@code
 *
 *   class CoffeeMaker {
 *     // tell the annotation processor Pump is provided externally at runtime
 *     // otherwise it thinks we have a missing dependency
 *     @External Pump pump;
 *   }
 * }</pre>
 * <p>
 * When building the BeanScope, the dependency must be provided manually via
 * {@link BeanScopeBuilder#bean(Class, Object)}.
 *
 * <pre>{@code
 *
 *   // provide external dependencies ...
 *   Pump pump = ...
 *
 *   try (BeanScope scope = BeanScope.builder()
 *     .bean(Pump.class, pump)
 *     .build()) {
 *
 *     CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
 *     coffeeMaker.makeIt()
 *   }
 *
 * }</pre>
 */
public interface BeanScope extends AutoCloseable {

  /**
   * Build a bean scope with options for shutdown hook and supplying external dependencies.
   * <p>
   * We can optionally:
   * <ul>
   *   <li>Provide external dependencies</li>
   *   <li>Specify a parent BeanScope</li>
   *   <li>Specify specific modules to wire</li>
   *   <li>Specify to include a shutdown hook (to fire preDestroy lifecycle methods)</li>
   *   <li>Use {@code forTesting()} to specify mocks and spies to use when wiring tests</li>
   * </ul>
   *
   * <pre>{@code
   *
   *   // create a BeanScope ...
   *
   *   try (BeanScope scope = BeanScope.builder()
   *     .build()) {
   *
   *     CoffeeMaker coffeeMaker = context.get(CoffeeMaker.class);
   *     coffeeMaker.makeIt()
   *   }
   *
   * }</pre>
   */
  static BeanScopeBuilder builder() {
    return new DBeanScopeBuilder();
  }

  /**
   * Return a single bean given the type.
   *
   * <pre>{@code
   *
   *   CoffeeMaker coffeeMaker = beanScope.get(CoffeeMaker.class);
   *   coffeeMaker.brew();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   * @throws java.util.NoSuchElementException When no matching bean is found
   */
  <T> T get(Class<T> type);

  /**
   * Return a single bean given the type and name.
   *
   * <pre>{@code
   *
   *   Heater heater = beanScope.get(Heater.class, "electric");
   *   heater.heat();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   * @param name the name qualifier of a specific bean
   * @throws java.util.NoSuchElementException When no matching bean is found
   */
  <T> T get(Class<T> type, @Nullable String name);

  /**
   * Return a single bean given the full generic type.
   *
   * @param type The generic type
   * @throws java.util.NoSuchElementException When no matching bean is found
   */
  default <T> T get(Type type) {
    return get(type, null);
  }

  /**
   * Return a single bean given the full generic type and name.
   *
   * @param type The generic type
   * @param name the name qualifier of a specific bean
   * @throws java.util.NoSuchElementException When no matching bean is found
   */
  <T> T get(Type type, @Nullable String name);

  /**
   * Optionally return a single bean given the type and empty if it is not found.
   *
   * @param type an interface or bean type
   */
  <T> Optional<T> getOptional(Class<T> type);

  /**
   * Optionally return a single bean given the type and name and empty if it is not found.
   *
   * @param type an interface or bean type
   * @param name the name qualifier of a specific bean
   */
  <T> Optional<T> getOptional(Type type, @Nullable String name);

  /**
   * Return the list of beans that have an annotation. The annotation must have a @Retention policy of RUNTIME
   *
   * <pre>{@code
   *
   *   // e.g. register all controllers with web a framework
   *   // .. where Controller is an annotation on the beans
   *
   *   List<Object> controllers = beanScope.listByAnnotation(Controller.class);
   *
   * }</pre>
   *
   * @param annotation An annotation class.
   */
  List<Object> listByAnnotation(Class<? extends Annotation> annotation);

  /**
   * Return the list of beans for a given type.
   *
   * <pre>{@code
   *
   *   // e.g. register all routes for a web framework
   *
   *   List<WebRoute> routes = beanScope.list(WebRoute.class);
   *
   * }</pre>
   *
   * @param type The type of beans to return.
   */
  <T> List<T> list(Class<T> type);

  /**
   * Return the list of beans that implement the given type.
   */
  <T> List<T> list(Type type);

  /** Return the list of beans that implement the class sorting by priority. */
  default <T> List<T> listByPriority(Class<T> type) {
    return listByPriority((Type) type);
  }

  /** Return the list of beans that implement the type sorting by priority. */
  <T> List<T> listByPriority(Type type);


  /**
   * Return the beans for this type mapped by their qualifier name.
   * <p>
   * Beans with no qualifier name get a generated unique key to use instead.
   */
  <T> Map<String, T> map(Type type);

  /**
   * Return all the bean entries from the scope.
   * <p>
   * The bean entries include entries from the parent scope if it has one.
   *
   * @return All bean entries from the scope.
   */
  List<BeanEntry> all();

  /**
   * Return true if the bean scope contains the given type.
   */
  boolean contains(Type type);

  /**
   * Return true if the bean scope contains the given type.
   */
  boolean contains(String type);

  /**
   * Close the scope firing any <code>@PreDestroy</code> lifecycle methods.
   */
  @Override
  void close();

  default Set<String> customScopeAnnotations() {
    return Set.of();
  }
}
