package io.avaje.inject;

import java.util.List;

/**
 * Deprecated - migrate to ApplicationScope.
 * <p>
 * Provides a global system wide BeanContext that contains all the bean contexts in the classpath.
 * <p>
 * This will automatically get all the bean contexts and wire them all as necessary. It will use
 * a shutdown hook to fire any <code>@PreDestroy</code> methods on beans.
 * </p>
 *
 * <h3>Example: get a bean</h3>
 * <pre>{@code
 *
 *   CoffeeMaker coffeeMaker = SystemContext.getBean(CoffeeMaker.class);
 *   coffeeMaker.brew();
 *
 * }</pre>
 *
 * <h3>Example: get all the beans implementing an interface</h3>
 * <pre>{@code
 *
 *   // e.g. register all WebRoutes for a web framework
 *
 *   List<WebRoute> routes = SystemContext.getBeans(WebRoute.class);
 *
 *   // register all the routes ...
 *
 * }</pre>
 *
 * <h3>Example: get all the beans that have an annotation</h3>
 * <pre>{@code
 *
 *   // e.g. register all controllers with web a framework
 *   // .. where Controller is an annotation on the beans
 *
 *   List<Object> controllers = SystemContext.getBeansWithAnnotation(Controller.class);
 *
 *   // register all the controllers ...
 *
 * }</pre>
 */
@Deprecated
public class SystemContext {

  private static final BeanContext rootContext = init();

  private static BeanContext init() {
    return BeanContext.newBuilder().build();
  }

  private SystemContext() {
    // hide
  }

  /**
   * Deprecated - migrate to ApplicationScope.scope().
   * <p>
   * Return the underlying BeanContext.
   */
  @Deprecated
  public static BeanContext context() {
    return ApplicationScope.scope();
  }

  /**
   * Deprecated - migrate to ApplicationScope.get().
   * <p>
   * Return a single bean given the type.
   *
   * <pre>{@code
   *
   *   CoffeeMaker coffeeMaker = SystemContext.getBean(CoffeeMaker.class);
   *   coffeeMaker.brew();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   */
  @Deprecated
  public static <T> T getBean(Class<T> type) {
    return ApplicationScope.get(type);
  }

  /**
   * Deprecated - migrate to ApplicationScope.get(type, name).
   * <p>
   * Return a single bean given the type and name.
   *
   * <pre>{@code
   *
   *   Heater heater = SystemContext.getBean(Heater.class, "electric");
   *   heater.heat();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   * @param name the name qualifier of a specific bean
   */
  @Deprecated
  public static <T> T getBean(Class<T> type, String name) {
    return ApplicationScope.get(type, name);
  }

  /**
   * Deprecated - removing support for this method.
   * <p>
   * Return the list of beans that have an annotation.
   *
   * <pre>{@code
   *
   *   // e.g. register all controllers with web a framework
   *   // .. where Controller is an annotation on the beans
   *
   *   List<Object> controllers = SystemContext.getBeansWithAnnotation(Controller.class);
   *
   * }</pre>
   *
   * @param annotation An annotation class.
   */
  @Deprecated
  public static List<Object> getBeansWithAnnotation(Class<?> annotation) {
    return rootContext.getBeansWithAnnotation(annotation);
  }

  /**
   * Deprecated - migrate to ApplicationScope.list().
   * <p>
   * Return the list of beans that implement the interface.
   *
   * <pre>{@code
   *
   *   // e.g. register all web routes with web a framework
   *
   *   List<WebRoute> routes = SystemContext.getBeans(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  @Deprecated
  public static <T> List<T> getBeans(Class<T> interfaceType) {
    return ApplicationScope.list(interfaceType);
  }

  /**
   * Deprecated - migrate to ApplicationScope.listByPriority().
   * <p>
   * Return the list of beans that implement the interface ordering based on <code>@Priority</code>.
   *
   * <pre>{@code
   *
   *   // e.g. register all web routes with web a framework
   *
   *   List<WebRoute> routes = SystemContext.getBeansByPriority(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  @Deprecated
  public static <T> List<T> getBeansByPriority(Class<T> interfaceType) {
    return ApplicationScope.listByPriority(interfaceType);
  }

}
