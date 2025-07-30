package io.avaje.inject.spi;

/** A Module containing dependencies that will be included in BeanScope. */
public interface AvajeModule extends InjectExtension {

  /** Empty array of strings. */
  String[] EMPTY_STRINGS = {};

  /**
   * Return public classes of the beans that would be registered by this module.
   *
   * <p>This method allows code to use reflection to inspect the modules classes before the module
   * is wired. This method is not required for DI wiring.
   */
  Class<?>[] classes();

  /** Build all the beans. */
  void build(Builder builder);

  /** Return the type names of types this module explicitly provides to other modules. */
  default String[] providesBeans() {
    return EMPTY_STRINGS;
  }

  /**
   * Return the type(s) of scopes that this module provides
   */
  default String[] definesScopes() {
    return EMPTY_STRINGS;
  }

  /**
   * Return the type names of types this module needs to be provided externally or via other
   * modules.
   */
  default String[] requiresBeans() {
    return EMPTY_STRINGS;
  }

  /** Return the type names of packages this module needs to be provided via other modules. */
  default String[] requiresPackagesFromType() {
    return EMPTY_STRINGS;
  }

  /** Marker for custom scoped modules. */
  interface Custom extends AvajeModule {}
}
