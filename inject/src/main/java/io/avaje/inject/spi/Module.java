package io.avaje.inject.spi;

import java.lang.reflect.Type;

import io.avaje.inject.InjectModule;

/**
 * A Module that can be included in BeanScope.
 *
 * @deprecated migrate to {@link AvajeModule}
 */
@Deprecated(forRemoval = true)
public interface Module extends AvajeModule {

  /**
   * Return the set of types this module explicitly provides to other modules.
   */
  @Override
  default Type[] provides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the types this module needs to be provided externally or via other modules.
   */
  @Override
  default Type[] requires() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the packages this module needs to be provided via other modules.
   */
  @Override
  default Type[] requiresPackages() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the classes that this module provides that we allow other modules to auto depend on.
   * <p>
   * This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link InjectModule#provides()}.
   */
  @Override
  default Type[] autoProvides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the aspects that this module provides.
   * <p>
   * This is a convenience when using multiple modules that we otherwise manually specify via
   * {@link InjectModule#provides()}.
   */
  @Override
  default Class<?>[] autoProvidesAspects() {
    return EMPTY_CLASSES;
  }

  /**
   * These are the classes that this module requires for wiring that are provided by other
   * external modules (that are in the classpath at compile time).
   * <p>
   * This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link InjectModule#requires()} or {@link InjectModule#requiresPackages()}.
   */
  @Override
  default Type[] autoRequires() {
    return EMPTY_CLASSES;
  }

  /**
   * These are the apects that this module requires whose implementations are provided by other external
   * modules (that are in the classpath at compile time).
   */
  @Override
  default Class<?>[] autoRequiresAspects() {
    return EMPTY_CLASSES;
  }

  /**
   * Return public classes of the beans that would be registered by this module.
   * <p>
   * This method allows code to use reflection to inspect the modules classes
   * before the module is wired. This method is not required for DI wiring.
   */
  @Override
  Class<?>[] classes();

  /**
   * Build all the beans.
   */
  @Override
  void build(Builder builder);

  /**
   * Marker for custom scoped modules.
   */
  interface Custom extends Module {

  }
}
