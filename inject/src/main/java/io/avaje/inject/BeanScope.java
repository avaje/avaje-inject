package io.avaje.inject;

import java.io.Closeable;
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
 *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
 *     coffeeMaker.makeIt()
 *   }
 *
 * }</pre>
 *
 * <h3>Implicitly used</h3>
 * <p>
 * The BeanScope is implicitly used by ApplicationScope.  It will be created as needed and
 * a shutdown hook will close the underlying BeanScope on JVM shutdown.
 * </p>
 * <pre>{@code
 *
 *   // BeanScope created as needed under the hood
 *
 *   CoffeeMaker coffeeMaker = ApplicationScope.get(CoffeeMaker.class);
 *   coffeeMaker.brew();
 *
 * }</pre>
 */
public interface BeanScope extends Closeable {

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
   * Create a RequestScope via builder where we provide extra instances
   * that can be used/included in wiring request scoped beans.
   *
   * <pre>{@code
   *
   *   try (RequestScope requestScope = beanScope.newRequestScope()
   *       // supply some instances
   *       .withBean(HttpRequest.class, request)
   *       .withBean(HttpResponse.class, response)
   *       .build()) {
   *
   *       MyController controller = requestScope.get(MyController.class);
   *       controller.process();
   *   }
   *
   *   ...
   *
   *   // define request scoped beans
   *   @Request
   *   MyController {
   *
   *     // can depend on supplied instances, singletons and other request scope beans
   *     @Inject
   *     MyController(HttpRequest request, HttpResponse response, MyService myService) {
   *       ...
   *     }
   *
   *   }
   *
   * }</pre>
   */
  RequestScopeBuilder newRequestScope();

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
   * Deprecated - migrate to get(type)
   */
  @Deprecated
  default <T> T getBean(Class<T> type) {
    return get(type);
  }

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
   * Deprecated - migrate to get(type, name).
   */
  @Deprecated
  default <T> T getBean(Class<T> type, String name) {
    return get(type, name);
  }

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
   * Deprecated - migrate to listByAnnotation()
   */
  @Deprecated
  default List<Object> getBeansWithAnnotation(Class<?> annotation) {
    return listByAnnotation(annotation);
  }

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
   * Deprecated - migrate to list(interfaceType).
   */
  @Deprecated
  default <T> List<T> getBeans(Class<T> interfaceType) {
    return list(interfaceType);
  }

  /**
   * Return the list of beans that implement the interface sorting by priority.
   */
  <T> List<T> listByPriority(Class<T> interfaceType);

  /**
   * Deprecated - migrate to listByPriority(interfaceType).
   */
  @Deprecated
  default <T> List<T> getBeansByPriority(Class<T> interfaceType) {
    return listByPriority(interfaceType);
  }

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
   * Deprecated - migrate to listByPriority().
   */
  @Deprecated
  default <T> List<T> getBeansByPriority(Class<T> interfaceType, Class<? extends Annotation> priority) {
    return listByPriority(interfaceType, priority);
  }

  /**
   * Return a request scoped provided for the specific type and name.
   *
   * @param type The type of the request scoped bean
   * @param name The optional qualifier name
   * @return The request scope provider or null
   */
  <T> RequestScopeMatch<T> requestProvider(Class<T> type, String name);

  /**
   * Close the scope firing any <code>@PreDestroy</code> methods.
   */
  void close();
}
