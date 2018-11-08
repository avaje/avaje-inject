package io.kanuka;

import java.util.List;

/**
 * Holds beans created by dependency injection.
 * <p>
 * The beans have singleton scope, support lifecycle methods for postConstruct and
 * preDestroy and are created (wired) via dependency injection.
 * </p>
 */
public interface BeanContext {

  /**
   * Return the name of the bean context.
   */
  String getName();

  /**
   * Return the names of modules this bean context depends on.
   */
  String[] getDependsOn();

  /**
   * Return a single bean given the type.
   */
  <T> T getBean(Class<T> beanClass);

  /**
   * Return a single bean given the type and name.
   */
  <T> T getBean(Class<T> beanClass, String name);

  /**
   * Return the list of beans that implement the interface or are marked with the annotation.
   *
   * @param interfaceOrAnnotation An interface class or annotation class.
   */
  List<Object> getBeans(Class<?> interfaceOrAnnotation);
}
