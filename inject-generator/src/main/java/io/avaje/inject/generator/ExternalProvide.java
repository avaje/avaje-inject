package io.avaje.inject.generator;

import io.avaje.inject.spi.Module;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * The types provided by other modules in the classpath at compile time.
 * <p>
 * When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvide {

  private final Set<String> providedTypes = new HashSet<>();

  void init() {
    for (Module module : ServiceLoader.load(Module.class)) {
      for (Class<?> provide : module.provides()) {
        providedTypes.add(provide.getCanonicalName());
      }
      for (Class<?> provide : module.autoProvides()) {
        providedTypes.add(provide.getCanonicalName());
      }
    }
  }

  /**
   * Return true if this type is provided by another module in the classpath.
   * We will add it to autoRequires().
   */
  boolean provides(String type) {
    return providedTypes.contains(type);
  }
}
