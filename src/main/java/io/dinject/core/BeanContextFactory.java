package io.dinject.core;

import io.dinject.BeanContext;

/**
 * This is the service loader interface defining the bean contexts that can be created.
 */
public interface BeanContextFactory {

  /**
   * Return the name of the bean context (module) this will create.
   */
  String getName();

  /**
   * Return the names of bean contexts (modules) that this is dependent on (they need to be built before this one).
   */
  String[] getDependsOn();

  /**
   * Create and return the BeanContext.
   */
  BeanContext createContext(Builder parent);
}
