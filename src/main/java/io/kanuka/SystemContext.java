package io.kanuka;

import java.util.List;

/**
 * Provides a global system wide BeanContext that contains all the bean contexts in the classpath.
 * <p>
 * This will automatically get all the bean contexts and wire them all as necessary. It will use
 * a shutdown hook to fire any <code>@PreDestroy</code> methods on beans.
 * </p>
 */
public class SystemContext {

  private static BeanContext rootContext = init();

  private static BeanContext init() {
    return new BootContext().load();
  }

  /**
   * Return a single bean given the type.
   */
  public static <T> T getBean(Class<T> cls) {
    return rootContext.getBean(cls);
  }

  /**
   * Return a single bean given the type and name.
   */
  public static <T> T getBean(Class<T> cls, String name) {
    return rootContext.getBean(cls, name);
  }

  /**
   * Return the list of beans that implement the interface or are marked with the annotation.
   *
   * @param interfaceOrAnnotation An interface class or annotation class.
   */
  public static List<Object> getBeans(Class<?> interfaceOrAnnotation) {
    return rootContext.getBeans(interfaceOrAnnotation);
  }

}
