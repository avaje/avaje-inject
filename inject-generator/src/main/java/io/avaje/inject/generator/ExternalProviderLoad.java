package io.avaje.inject.generator;

import io.avaje.inject.spi.Module;
import java.util.*;

final class ExternalProviderLoad {

  /**
   * Return the components provided by external modules in the classpath.
   */
  static List<String> load() {
    List<String> externallyProvided = new ArrayList<>();
    try {
      ServiceLoader<Module> load = ServiceLoader.load(Module.class, ExternalProvider.class.getClassLoader());
      Iterator<Module> iterator = load.iterator();
      while (iterator.hasNext()) {
        try {
          Module module = iterator.next();
          for (final Class<?> provide : module.provides()) {
            externallyProvided.add(provide.getCanonicalName());
          }
          for (final Class<?> provide : module.autoProvides()) {
            externallyProvided.add(provide.getCanonicalName());
          }
          for (final Class<?> provide : module.autoProvidesAspects()) {
            externallyProvided.add(Util.wrapAspect(provide.getCanonicalName()));
          }
        } catch (final ServiceConfigurationError expected) {
          // ignore expected error reading the module that we are also writing
        }
      }
    } catch (Throwable e) {
      // ignore
    }
    return externallyProvided;
  }
}
