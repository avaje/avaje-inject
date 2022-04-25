package io.avaje.inject.spi;

/**
 * A Module that can be included in BeanScope.
 */
public interface Module {

  /**
   * Return the set of types this module explicitly provides to other modules.
   */
  Class<?>[] provides();

  /**
   * Return the types this module needs to be provided externally or via other modules.
   */
  Class<?>[] requires();

  /**
   * Return the packages this module needs to be provided via other modules.
   */
  Class<?>[] requiresPackages();

  /**
   * Return public classes of the beans that would be registered by this module.
   * <p>
   * This method allows code to use reflection to inspect the modules classes
   * before the module is wired. This method is not required for DI wiring.
   */
  Class<?>[] classes();

  /**
   * Build all the beans.
   */
  void build(Builder builder);

  /**
   * Marker for custom scoped modules.
   */
  interface Custom extends Module {

  }
}
