package io.avaje.inject.spi;

/**
 * This is the service loader interface defining the bean scope.
 */
public interface BeanScopeFactory {

  /**
   * Return the name of the bean scope (module) this will create.
   */
  String getName();

  /**
   * Return the name of module features this module provides.
   */
  String[] getProvides();

  /**
   * Return the names of bean scopes (modules) that this is dependent on (they need to be built before this one).
   */
  String[] getDependsOn();

  /**
   * Build all the beans.
   */
  void build(Builder builder);
}
