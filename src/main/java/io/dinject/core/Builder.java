package io.dinject.core;

import io.dinject.BeanContext;
import io.dinject.BeanEntry;

import java.util.List;
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
   * Return the names of module features that this module provides.
   */
  String[] getProvides();

  /**
   * Return the names of modules that this module depends on.
   */
  String[] getDependsOn();

  /**
   * Set a parent builder that can provide cross-module dependencies.
   */
  void setParent(Builder parent);

  /**
   * Return true if the bean should be created and registered with the context.
   * <p/>
   * Returning false means there has been a (test double) bean already registered and
   * that we should skip the creation and registration for this bean.
   *
   * @param addForType   The interface that the bean implements and provides
   * @param injectTarget The actual bean type we are looking to create and register
   */
  boolean isAddBeanFor(Class<?> addForType, Class<?> injectTarget);

  /**
   * Return true if the bean should be created and registered with the context.
   * <p/>
   * Returning false means there has been a (test double) bean already registered and
   * that we should skip the creation and registration for this bean.
   *
   * @param injectTarget The actual bean type we are looking to create and register
   */
  boolean isAddBeanFor(Class<?> injectTarget);

  /**
   * Register the bean instance into the context.
   * <p>
   * Beans are added in an appropriate order to satisfy dependencies.
   * </p>
   *
   * @param bean  The bean instance that has been created.
   * @param name  The (optional) name of the instance.
   * @param types Interfaces and class level annotations this bean provides or associates to.
   */
  void register(Object bean, String name, Class<?>... types);

  /**
   * Register the bean as a Primary bean.
   */
  void registerPrimary(Object bean, String name, Class<?>... types);

  /**
   * Register the bean as a secondary bean.
   */
  void registerSecondary(Object bean, String name, Class<?>... types);

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
   * Get a list of dependencies for the interface type .
   */
  <T> List<T> getList(Class<T> interfaceType);

  /**
   * Get a candidate dependency allowing it to be null.
   */
  <T> BeanEntry<T> candidate(Class<T> cls, String name);

  /**
   * Build and return the bean context.
   */
  BeanContext build();

}
