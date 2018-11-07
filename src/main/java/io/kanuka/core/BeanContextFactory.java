package io.kanuka.core;

import io.kanuka.BeanContext;

/**
 * This is the service loader interface defining the bean contexts that can be created.
 */
public interface BeanContextFactory {

  /**
   * Return the name of the bean context this will create.
   */
  String name();

  /**
   * Create and return the BeanContext.
   */
  BeanContext createContext(Builder parent);
}
