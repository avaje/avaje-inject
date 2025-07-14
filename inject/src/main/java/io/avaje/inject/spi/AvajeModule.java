package io.avaje.inject.spi;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/** A Module containing dependencies that will be included in BeanScope. */
public interface AvajeModule extends InjectExtension {

  /** Empty array of classes. */
  @Deprecated(forRemoval = true)
  Class<?>[] EMPTY_CLASSES = {};

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
  
  /**
   * Return the set of types this module explicitly provides to other modules.
   *
   * @deprecated use {@link #providesBeans()}
   */
  @Deprecated(forRemoval = true)
  default Type[] provides() {
    return EMPTY_CLASSES;
  }

  /** Return the type names of types this module explicitly provides to other modules. */
  default String[] providesBeans() {
    return Arrays.stream(Objects.requireNonNullElse(provides(), EMPTY_CLASSES))
        .map(Type::getTypeName)
        .toArray(String[]::new);
  }

  /**
   * Return the types this module needs to be provided externally or via other modules.
   *
   * @deprecated use {@link #requiresBeans()}
   */
  @Deprecated(forRemoval = true)
  default Type[] requires() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the type names of types this module needs to be provided externally or via other
   * modules.
   */
  default String[] requiresBeans() {
    return Arrays.stream(Objects.requireNonNullElse(requires(), EMPTY_CLASSES))
        .map(Type::getTypeName)
        .toArray(String[]::new);
  }

  /**
   * Return the packages this module needs to be provided via other modules.
   *
   * @deprecated use {@link #requiresPackagesFromType()}
   */
  @Deprecated(forRemoval = true)
  default Type[] requiresPackages() {
    return EMPTY_CLASSES;
  }

  /** Return the type names of packages this module needs to be provided via other modules. */
  default String[] requiresPackagesFromType() {
    return Arrays.stream(Objects.requireNonNullElse(requiresPackages(), EMPTY_CLASSES))
        .map(Type::getTypeName)
        .toArray(String[]::new);
  }

  /**
   * Return the classes that this module provides that we allow other modules to auto depend on.
   *
   * <p>This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link AvajeModule#provides()}.
   *
   * @deprecated use {@link #autoProvidesBeans()}
   */
  @Deprecated(forRemoval = true)
  default Type[] autoProvides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the type names of classes that this module provides that we allow other modules to auto
   * depend on.
   */
  default String[] autoProvidesBeans() {
    return Arrays.stream(Objects.requireNonNullElse(autoProvides(), EMPTY_CLASSES))
        .map(Type::getTypeName)
        .toArray(String[]::new);
  }

  /**
   * Return the aspects that this module provides.
   *
   * <p>This is a convenience when using multiple modules that we otherwise manually specify via
   * {@link AvajeModule#provides()}.
   */
  @Deprecated(forRemoval = true)
  default Class<?>[] autoProvidesAspects() {
    return EMPTY_CLASSES;
  }

  /** Return the type names of aspects that this module provides. */
  default String[] autoProvidesAspectBeans() {
    return Arrays.stream(Objects.requireNonNullElse(autoProvidesAspects(), EMPTY_CLASSES))
        .map(Class::getTypeName)
        .toArray(String[]::new);
  }

  /**
   * These are the classes that this module requires for wiring that are provided by other external
   * modules (that are in the classpath at compile time).
   *
   * <p>This is a convenience when using multiple modules that is otherwise controlled manually by
   * explicitly using {@link AvajeModule#requires()} or {@link AvajeModule#requiresPackages()}.
   */
  @Deprecated(forRemoval = true)
  default Type[] autoRequires() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the type names of classes that this module requires for wiring that are provided by
   * other external modules.
   */
  default String[] autoRequiresBeans() {
    return Arrays.stream(Objects.requireNonNullElse(autoRequires(), EMPTY_CLASSES))
        .map(Type::getTypeName)
        .toArray(String[]::new);
  }

  /**
   * These are the aspects that this module requires whose implementations are provided by other
   * external modules (that are in the classpath at compile time).
   */
  @Deprecated(forRemoval = true)
  default Class<?>[] autoRequiresAspects() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the type names of aspects that this module requires whose implementations are provided
   * by other external modules.
   */
  default String[] autoRequiresAspectBeans() {
    return Arrays.stream(Objects.requireNonNullElse(autoRequiresAspects(), EMPTY_CLASSES))
        .map(Class::getTypeName)
        .toArray(String[]::new);
  }

  /** Marker for custom scoped modules. */
  interface Custom extends AvajeModule {}
}
