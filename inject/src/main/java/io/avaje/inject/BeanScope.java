package io.avaje.inject;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Holds beans created by dependency injection.
 * <p>
 * The beans have singleton scope, support lifecycle methods for postConstruct and
 * preDestroy and are created (wired) via dependency injection.
 * </p>
 *
 * <h3>Create a BeanScope</h3>
 * <p>
 * We can programmatically create a BeanScope via BeanScopeBuilder.
 * </p>
 * <pre>{@code
 *
 *   // create a BeanScope ...
 *
 *   try (BeanScope scope = BeanScope.newBuilder()
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
   * Build a bean scope with options for shutdown hook and supplying test doubles.
   * <p>
   * We would choose to use BeanScopeBuilder in test code (for component testing)
   * as it gives us the ability to inject test doubles, mocks, spy's etc.
   * </p>
   *
   * <pre>{@code
   *
   *   @Test
   *   public void someComponentTest() {
   *
   *     MyRedisApi mockRedis = mock(MyRedisApi.class);
   *     MyDbApi mockDatabase = mock(MyDbApi.class);
   *
   *     try (BeanScope scope = BeanScope.newBuilder()
   *       .withBeans(mockRedis, mockDatabase)
   *       .build()) {
   *
   *       // built with test doubles injected ...
   *       CoffeeMaker coffeeMaker = scope.get(CoffeeMaker.class);
   *       coffeeMaker.makeIt();
   *
   *       assertThat(...
   *     }
   *   }
   * }</pre>
   */
  static BeanScopeBuilder newBuilder() {
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
   */
  <T> T get(Class<T> type, String name);

  /**
   * Return the list of beans that have an annotation.
   *
   * <pre>{@code
   *
   *   // e.g. register all controllers with web a framework
   *   // .. where Controller is an annotation on the beans
   *
   *   List<Object> controllers = ApplicationScope.listByAnnotation(Controller.class);
   *
   * }</pre>
   *
   * <p>
   * The classic use case for this is registering controllers or routes to
   * web frameworks like Sparkjava, Javlin, Rapidoid etc.
   *
   * @param annotation An annotation class.
   */
  List<Object> listByAnnotation(Class<?> annotation);

  /**
   * Return the list of beans that implement the interface.
   *
   * <pre>{@code
   *
   *   // e.g. register all routes for a web framework
   *
   *   List<WebRoute> routes = ApplicationScope.list(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  <T> List<T> list(Class<T> interfaceType);

  /**
   * Return the list of beans that implement the interface sorting by priority.
   */
  <T> List<T> listByPriority(Class<T> interfaceType);

  /**
   * Return the beans that implement the interface sorting by the priority annotation used.
   * <p>
   * The priority annotation will typically be either <code>javax.annotation.Priority</code>
   * or <code>jakarta.annotation.Priority</code>.
   *
   * @param interfaceType The interface type of the beans to return
   * @param priority      The priority annotation used to sort the beans
   */
  <T> List<T> listByPriority(Class<T> interfaceType, Class<? extends Annotation> priority);

  /**
   * Close the scope firing any <code>@PreDestroy</code> methods.
   */
  void close();
}
