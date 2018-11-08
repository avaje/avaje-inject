package io.kanuka.core;

import io.kanuka.BeanContext;

import java.util.Optional;

/**
 * Mutable builder object used when building the context.
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
  void addLifecycle(BeanLifeCycle lifeCycleBean);

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
   * Fire post construct on all beans in the context.
   */
  void postConstruct();

  /**
   * Build and return the bean context.
   */
  BeanContext build();


}
