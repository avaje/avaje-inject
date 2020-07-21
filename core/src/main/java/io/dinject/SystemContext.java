package io.dinject;

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
public class SystemContext {

  private static final BeanContext rootContext = init();

  private static BeanContext init() {
    return new BootContext().load();
  }

  private SystemContext() {
    // hide
  }

  /**
   * Return the underlying BeanContext.
   */
  public static BeanContext context() {
    return rootContext;
  }

  /**
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
  public static <T> T getBean(Class<T> type) {
    return rootContext.getBean(type);
  }

  /**
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
  public static <T> T getBean(Class<T> type, String name) {
    return rootContext.getBean(type, name);
  }

  /**
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
  public static List<Object> getBeansWithAnnotation(Class<?> annotation) {
    return rootContext.getBeansWithAnnotation(annotation);
  }

  /**
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
  public static <T> List<T> getBeans(Class<T> interfaceType) {
    return rootContext.getBeans(interfaceType);
  }

  /**
   * Return the list of beans that implement the interface without ordering based on <code>@Priority</code>.
   *
   * <pre>{@code
   *
   *   // e.g. register all web routes with web a framework
   *
   *   List<WebRoute> routes = SystemContext.getBeansUnsorted(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  public static <T> List<T> getBeansUnsorted(Class<T> interfaceType) {
    return rootContext.getBeansUnsorted(interfaceType);
  }

}
