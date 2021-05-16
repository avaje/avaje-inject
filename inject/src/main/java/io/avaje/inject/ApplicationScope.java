package io.avaje.inject;

import java.util.List;

/**
 * Provides a global system wide BeanContext that contains all the bean contexts in the classpath.
 * <p>
 * This will automatically get all the bean contexts and wire them all as necessary. It will use
 * a shutdown hook to fire any <code>@PreDestroy</code> methods on beans.
 * </p>
 *
 * <h3>Example: get a bean</h3>
 * <pre>{@code
 *
 *   CoffeeMaker coffeeMaker = ApplicationScope.get(CoffeeMaker.class);
 *   coffeeMaker.brew();
 *
 * }</pre>
 *
 * <h3>Example: get all the beans implementing an interface</h3>
 * <pre>{@code
 *
 *   // e.g. register all WebRoutes for a web framework
 *
 *   List<WebRoute> routes = ApplicationScope.list(WebRoute.class);
 *
 *   // register all the routes ...
 *
 * }</pre>
 */
public class ApplicationScope {

  private static final BeanScope appScope = init();

  private static BeanScope init() {
    return BeanScope.newBuilder().build();
  }

  private ApplicationScope() {
    // hide
  }

  /**
   * Return the underlying BeanContext.
   */
  public static BeanScope scope() {
    return appScope;
  }

  /**
   * Return a single bean given the type.
   *
   * <pre>{@code
   *
   *   CoffeeMaker coffeeMaker = ApplicationScope.get(CoffeeMaker.class);
   *   coffeeMaker.brew();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   */
  public static <T> T get(Class<T> type) {
    return appScope.get(type);
  }

  /**
   * Return a single bean given the type and name.
   *
   * <pre>{@code
   *
   *   Heater heater = ApplicationScope.get(Heater.class, "electric");
   *   heater.heat();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   * @param name the name qualifier of a specific bean
   */
  public static <T> T get(Class<T> type, String name) {
    return appScope.get(type, name);
  }


  /**
   * Return the list of beans that implement the interface.
   *
   * <pre>{@code
   *
   *   // e.g. register all web routes with web a framework
   *
   *   List<WebRoute> routes = ApplicationScope.list(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  public static <T> List<T> list(Class<T> interfaceType) {
    return appScope.list(interfaceType);
  }

  /**
   * Return the list of beans that implement the interface ordering based on <code>@Priority</code>.
   *
   * <pre>{@code
   *
   *   // e.g. register all web routes with web a framework
   *
   *   List<WebRoute> routes = ApplicationScope.listByPriority(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  public static <T> List<T> listByPriority(Class<T> interfaceType) {
    return appScope.listByPriority(interfaceType);
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
   * web frameworks like Sparkjava, Javalin, Rapidoid etc.
   *
   * @param annotation An annotation class.
   */
  public static List<Object> listByAnnotation(Class<?> annotation) {
    return appScope.listByAnnotation(annotation);
  }

  /**
   * Start building a RequestScope.
   *
   * @return The request scope builder
   */
  public static RequestScopeBuilder newRequestScope() {
    return appScope.newRequestScope();
  }
}
