package io.avaje.inject.spi;

import java.util.List;
import java.util.Set;

/**
 * Determines Multi-Module wiring order.
 */
public interface ModuleOrdering extends InjectExtension {

  /**
   * Return true if ordering supports the modules passed in.
   */
  default boolean supportsExpected(List<AvajeModule> modules) {
    return true;
  }

  /**
   * Accept a module for ordering
   */
  void add(AvajeModule module);

  /**
   * Order the factories, returning the ordered list of module names.
   */
  Set<String> orderModules();

  /**
   * The list of factories in the order they should be built.
   */
  List<AvajeModule> factories();

  /**
   * Whether no modules are available
   */
  boolean isEmpty();

}
