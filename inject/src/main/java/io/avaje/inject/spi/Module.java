package io.avaje.inject.spi;

/**
 * A Module that can be included in BeanScope.
 *
 * @deprecated migrate to {@link InjectModule}
 */
@Deprecated(forRemoval = true)
public interface Module extends InjectModule {

  /**
   * Return the set of types this module explicitly provides to other modules.
   */
  @Override
  default Class<?>[] provides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the types this module needs to be provided externally or via other modules.
   */
  @Override
  default Class<?>[] requires() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the packages this module needs to be provided via other modules.
   */
  @Override
  default Class<?>[] requiresPackages() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the classes that this module provides that we allow other modules to auto depend on.
   * <p>
   * This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link io.avaje.inject.InjectModule#provides()}.
   */
  @Override
  default Class<?>[] autoProvides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the aspects that this module provides.
   * <p>
   * This is a convenience when using multiple modules that we otherwise manually specify via
   * {@link io.avaje.inject.InjectModule#provides()}.
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
   * explicitly using {@link io.avaje.inject.InjectModule#requires()} or {@link io.avaje.inject.InjectModule#requiresPackages()}.
   */
  @Override
  default Class<?>[] autoRequires() {
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
