package io.avaje.inject.spi;

import java.lang.reflect.Type;

/**
 * A AvajeModule that can be included in BeanScope.
 */
public interface InjectModule extends InjectExtension {

  /**
   * Empty array of classes.
   */
  Class<?>[] EMPTY_CLASSES = {};

  /**
   * Return the set of types this module explicitly provides to other modules.
   */
  default Type[] provides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the types this module needs to be provided externally or via other modules.
   */
  default Type[] requires() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the packages this module needs to be provided via other modules.
   */
  default Type[] requiresPackages() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the classes that this module provides that we allow other modules to auto depend on.
   *
   * <p>This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link InjectModule#provides()}.
   */
  default Type[] autoProvides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the aspects that this module provides.
   *
   * <p>This is a convenience when using multiple modules that we otherwise manually specify via
   * {@link InjectModule#provides()}.
   */
  default Class<?>[] autoProvidesAspects() {
    return EMPTY_CLASSES;
  }

  /**
   * These are the classes that this module requires for wiring that are provided by other external
   * modules (that are in the classpath at compile time).
   *
   * <p>This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link InjectModule#requires()} or {@link InjectModule#requiresPackages()}.
   */
  default Type[] autoRequires() {
    return EMPTY_CLASSES;
  }

  /**
   * These are the aspects that this module requires whose implementations are provided by other
   * external modules (that are in the classpath at compile time).
   */
  default Class<?>[] autoRequiresAspects() {
    return EMPTY_CLASSES;
  }

  /**
   * Return public classes of the beans that would be registered by this module.
   *
   * <p>This method allows code to use reflection to inspect the modules classes before the module
   * is wired. This method is not required for DI wiring.
   */
  Class<?>[] classes();

  /**
   * Build all the beans.
   */
  void build(Builder builder);

  /**
   * Marker for custom scoped modules.
   */
  interface Custom extends InjectModule {
  }
}
