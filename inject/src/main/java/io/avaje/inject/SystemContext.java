package io.avaje.inject;

import java.util.List;

/**
 * Deprecated - migrate to ApplicationScope.
 * <p>
 * Provides a global system wide BeanScope that contains all the bean contexts in the classpath.
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
 */
@Deprecated
public class SystemContext {

  private SystemContext() {
    // hide
  }

  /**
   * Deprecated - migrate to ApplicationScope.scope().
   */
  @Deprecated
  public static BeanScope context() {
    return ApplicationScope.scope();
  }

  /**
   * Deprecated - migrate to ApplicationScope.get(type).
   */
  @Deprecated
  public static <T> T getBean(Class<T> type) {
    return ApplicationScope.get(type);
  }

  /**
   * Deprecated - migrate to ApplicationScope.get(type, name).
   */
  @Deprecated
  public static <T> T getBean(Class<T> type, String name) {
    return ApplicationScope.get(type, name);
  }

  /**
   * Deprecated - migrate to ApplicationScope.listByAnnotation(annotation).
   */
  @Deprecated
  public static List<Object> getBeansWithAnnotation(Class<?> annotation) {
    return ApplicationScope.listByAnnotation(annotation);
  }

  /**
   * Deprecated - migrate to ApplicationScope.list().
   */
  @Deprecated
  public static <T> List<T> getBeans(Class<T> interfaceType) {
    return ApplicationScope.list(interfaceType);
  }

  /**
   * Deprecated - migrate to ApplicationScope.listByPriority().
   */
  @Deprecated
  public static <T> List<T> getBeansByPriority(Class<T> interfaceType) {
    return ApplicationScope.listByPriority(interfaceType);
  }

}
