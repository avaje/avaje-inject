package io.avaje.inject.generator;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.Plugin;

/**
 * The types provided by other modules in the classpath at compile time.
 *
 * <p>When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvider {

  private static final boolean injectAvailable = moduleCP();

  private ExternalProvider() {}

  private static boolean moduleCP() {
    try {
      Class.forName(Constants.MODULE);
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  static void registerModuleProvidedTypes(Set<String> providedTypes) {
    if (!injectAvailable) {
      return;
    }

    Iterator<Module> iterator = ServiceLoader.load(Module.class, ExternalProvider.class.getClassLoader()).iterator();
    while (iterator.hasNext()) {
      try {
        Module module = iterator.next();
        for (final Class<?> provide : module.provides()) {
          providedTypes.add(provide.getCanonicalName());
        }
        for (final Class<?> provide : module.autoProvides()) {
          providedTypes.add(provide.getCanonicalName());
        }
        for (final Class<?> provide : module.autoProvidesAspects()) {
          providedTypes.add(Util.wrapAspect(provide.getCanonicalName()));
        }
      } catch (final ServiceConfigurationError expected) {
        // ignore expected error reading the module that we are also writing
      }
    }
  }

  /**
   * Register types provided by the plugin so no compiler error when we have a dependency on these
   * types and the only thing providing them is the plugin.
   */
  static void registerPluginProvidedTypes(ScopeInfo defaultScope) {
    if (!injectAvailable) {
      return;
    }
    for (final Plugin plugin : ServiceLoader.load(Plugin.class, Processor.class.getClassLoader())) {
      for (final Class<?> provide : plugin.provides()) {
        defaultScope.pluginProvided(provide.getCanonicalName());
      }
      for (final Class<?> provide : plugin.providesAspects()) {
        defaultScope.pluginProvided(Util.wrapAspect(provide.getCanonicalName()));
      }
    }
  }
}
