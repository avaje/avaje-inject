package io.avaje.inject.spi;

import java.util.List;
import java.util.Set;

/**
 * Determines Wiring order.
 */
public interface ModuleOrdering {

  /**
   * Order the factories, returning the ordered list of module names.
   */
  Set<String> orderModules();

  /**
   * The list of factories in the order they should be built.
   */
  List<Module> factories();

  /**
   * Whether no modules are available
   */
  boolean isEmpty();

  /**
   * Accept a module for ordering
   */
  void add(Module module);
}
