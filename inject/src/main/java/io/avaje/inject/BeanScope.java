package io.avaje.inject;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
 * We can supporting external dependencies when creating the BeanScope. We need to do 2 things.
 * we need to specify these via
 * </p>
 * <ul>
 *   <li>
 *       1. Specify the external dependency via {@code @InjectModule(requires=...)}.
 *       Otherwise at compile time the annotation processor detects it as a missing dependency and we can't compile.
 *   </li>
 *   <li>
 *       2. Provide the dependency when creating the BeanScope
 *   </li>
 * </ul>
 * <p>
 * For example, given we have Pump as an externally provided dependency.
 *
 * <pre>{@code
 *
 *   // tell the annotation processor Pump is provided externally
 *   // otherwise it thinks we have a missing dependency
 *
 *   @InjectModule(requires=Pump.class)
 *
 * }</pre>
 * <p>
 * When we build the BeanScope provide the dependency via {@link BeanScopeBuilder#bean(Class, Object)}.
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
@NonNullApi
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
   * Deprecated - migrate to builder().
   */
  @Deprecated
  static BeanScopeBuilder newBuilder() {
    return builder();
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
   * Return a single bean given the generic type and name.
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

  /**
   * Return the list of beans that implement the interface sorting by priority.
   */
  <T> List<T> listByPriority(Class<T> type);

  /**
   * Return the beans that implement the interface sorting by the priority annotation used.
   * <p>
   * The priority annotation will typically be either <code>javax.annotation.Priority</code>
   * or <code>jakarta.annotation.Priority</code>.
   *
   * @param type     The interface type of the beans to return
   * @param priority The priority annotation used to sort the beans
   */
  <T> List<T> listByPriority(Class<T> type, Class<? extends Annotation> priority);

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
}
