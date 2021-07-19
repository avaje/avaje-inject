package io.avaje.inject.spi;

/**
 * This is the service loader interface defining the bean scope.
 */
public interface Module {

  /**
   * Return the types this module needs to be provided externally or via other modules.
   */
  Class<?>[] requires();

  /**
   * Return the set of types this module explicitly provides to other modules.
   */
  Class<?>[] provides();

  /**
   * Build all the beans.
   */
  void build(Builder builder);
}
