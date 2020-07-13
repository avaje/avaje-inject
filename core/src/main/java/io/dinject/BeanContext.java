package io.dinject;

import io.dinject.annotation.ContextModule;

import java.io.Closeable;
import java.util.List;

/**
 * Holds beans created by dependency injection.
 * <p>
 * The beans have singleton scope, support lifecycle methods for postConstruct and
 * preDestroy and are created (wired) via dependency injection.
 * </p>
 *
 * <h3>Create a BeanContext</h3>
 * <p>
 * We can programmatically create a BeanContext via BootContext.
 * </p>
 * <pre>{@code
 *
 *   // create a BeanContext ...
 *
 *   try (BeanContext context = new BootContext()
 *     .load()) {
 *
 *     CoffeeMaker coffeeMaker = context.getBean(CoffeeMaker.class);
 *     coffeeMaker.makeIt()
 *   }
 *
 * }</pre>
 *
 * <h3>Implicitly used</h3>
 * <p>
 * The BeanContext is implicitly used by SystemContext.  It will be created as needed and
 * a shutdown hook will close the underlying BeanContext on JVM shutdown.
 * </p>
 * <pre>{@code
 *
 *   // BeanContext created as needed under the hood
 *
 *   CoffeeMaker coffeeMaker = SystemContext.getBean(CoffeeMaker.class);
 *   coffeeMaker.brew();
 *
 * }</pre>
 */
public interface BeanContext extends Closeable {

  /**
   * Return the module name of the bean context.
   *
   * @see ContextModule
   */
  String getName();

  /**
   * Return the names of module features this bean context provides.
   *
   * @see ContextModule
   */
  String[] getProvides();

  /**
   * Return the names of modules this bean context depends on.
   *
   * @see ContextModule
   */
  String[] getDependsOn();

  /**
   * Return a single bean given the type.
   *
   * <pre>{@code
   *
   *   CoffeeMaker coffeeMaker = beanContext.getBean(CoffeeMaker.class);
   *   coffeeMaker.brew();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   */
  <T> T getBean(Class<T> type);

  /**
   * Return a single bean given the type and name.
   *
   * <pre>{@code
   *
   *   Heater heater = beanContext.getBean(Heater.class, "electric");
   *   heater.heat();
   *
   * }</pre>
   *
   * @param type an interface or bean type
   * @param name the name qualifier of a specific bean
   */
  <T> T getBean(Class<T> type, String name);

  /**
   * Return the wiring candidate bean with name and priority.
   */
  <T> BeanEntry<T> candidate(Class<T> type, String name);

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
   * <p>
   * The classic use case for this is registering controllers or routes to
   * web frameworks like Sparkjava, Javlin, Rapidoid etc.
   *
   * @param annotation An annotation class.
   */
  List<Object> getBeansWithAnnotation(Class<?> annotation);

  /**
   * Return the list of beans that implement the interface.
   *
   * <pre>{@code
   *
   *   // e.g. register all routes for a web framework
   *
   *   List<WebRoute> routes = SystemContext.getBeans(WebRoute.class);
   *
   * }</pre>
   *
   * @param interfaceType An interface class.
   */
  <T> List<T> getBeans(Class<T> interfaceType);

  /**
   * Return the list of beans that implement the interface without
   * sorting by priority (ignoring any Priority annotation).
   */
  <T> List<T> getBeansUnsorted(Class<T> interfaceType);

  /**
   * Sort the beans by javax.annotation.Priority annotation.
   *
   * @param list The beans to sort by priority
   * @return A new list of beans sorted by priority
   */
  <T> List<T> sortByPriority(List<T> list);

  /**
   * Start the context firing any <code>@PostConstruct</code> methods.
   */
  void start();

  /**
   * Close the context firing any <code>@PreDestroy</code> methods.
   */
  void close();
}
