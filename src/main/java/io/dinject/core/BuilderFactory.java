package io.dinject.core;

import java.util.List;

/**
 * Factory for creating Builder instances.
 * <p>
 * These Builders are typically used by generated code (Java annotation processing - dinject-generator).
 */
public class BuilderFactory {

  /**
   * Create the root level Builder.
   *
   * @param suppliedBeans The list of beans (typically test doubles) supplied when building the context.
   */
  public static Builder newRootBuilder(List<SuppliedBean> suppliedBeans) {
    return new DBuilder(suppliedBeans);
  }

  /**
   * Create a Builder for the named context (module).
   *
   * @param name      the name of the module / bean context
   * @param provides  the module features this module provides
   * @param dependsOn the names of modules this module is depends on.
   */
  public static Builder newBuilder(String name, String[] provides, String[] dependsOn) {
    return new DBuilder(name, provides, dependsOn);
  }
}
