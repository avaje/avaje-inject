package io.avaje.inject.generator;

import io.avaje.inject.spi.Module;

import java.util.*;

/**
 * The types provided by other modules in the classpath at compile time.
 * <p>
 * When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvider {

  private final Set<String> providedTypes = new HashSet<>();

  void init() {
    ServiceLoader<Module> load = ServiceLoader.load(Module.class, ExternalProvider.class.getClassLoader());
    Iterator<Module> iterator = load.iterator();
    while (iterator.hasNext()) {
      try {
        Module module = iterator.next();
        for (final Class<?> provide : module.provides()) {
          providedTypes.add(provide.getCanonicalName());
        }
        for (Class<?> provide : module.autoProvides()) {
          providedTypes.add(provide.getCanonicalName());
        }
        for (final Class<?> provide : module.autoProvidesAspects()) {
          providedTypes.add(Util.wrapAspect(provide.getCanonicalName()));
        }
      } catch (ServiceConfigurationError expected) {
        // ignore expected error reading the module that we are also writing
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
