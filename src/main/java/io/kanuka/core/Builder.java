package io.kanuka.core;

import io.kanuka.BeanContext;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Mutable builder object used when building a bean context.
 */
public interface Builder {

  /**
   * Return the name of the (module) context this builder is creating.
   */
  String getName();

  /**
   * Return the names of modules that this module depends on.
   */
  String[] getDependsOn();

  /**
   * Set a parent builder that can provide cross-module dependencies.
   */
  void setParent(Builder parent);

  /**
   * Return true if the bean should be added. Returning false means the bean is already supplied
   * to the context (typically a test double bean).
   */
  boolean isAddBeanFor(String type);

  /**
   * Add a bean instance to the context.
   * <p>
   * Beans are added in an appropriate order to satisfy dependencies.
   * </p>
   *
   * @param bean           The bean instance that has been created.
   * @param name           The (optional) name of the instance.
   * @param interfaceClass Interfaces and class level annotations this bean provides or associates to.
   */
  void addBean(Object bean, String name, String... interfaceClass);

  /**
   * Add a lifecycle bean.
   */
  void addLifecycle(BeanLifecycle bean);

  /**
   * Add a field injector.
   */
  void addInjector(Consumer<Builder> injector);

  /**
   * Add a child context.
   */
  void addChild(BeanContext context);

  /**
   * Set the type of the current bean being created (to assist in error messages when injecting dependencies).
   */
  void currentBean(String currentBean);

  /**
   * Get an optional dependency.
   */
  <T> Optional<T> getOptional(Class<T> cls);

  /**
   * Get an optional named dependency.
   */
  <T> Optional<T> getOptional(Class<T> cls, String name);

  /**
   * Get a dependency.
   */
  <T> T get(Class<T> cls);

  /**
   * Get a named dependency.
   */
  <T> T get(Class<T> cls, String name);

  /**
   * Get a named dependency allowing it to be null.
   */
  <T> T getMaybe(Class<T> cls, String name);

  /**
   * Build and return the bean context.
   */
  BeanContext build();

}
