package io.avaje.inject.generator;

import io.avaje.inject.spi.Module;
import io.avaje.inject.spi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

final class LoadServices {

  static List<AvajeModule> loadModules(ClassLoader classLoader) {
    List<AvajeModule> modules = new ArrayList<>();
    // load using older Module
    ServiceLoader.load(Module.class, classLoader).forEach(modules::add);
    // load newer AvajeModule
    final var iterator = ServiceLoader.load(InjectExtension.class, classLoader).iterator();
    while (iterator.hasNext()) {
      try {
        final var spi = iterator.next();
        if (spi instanceof AvajeModule) {
          modules.add((AvajeModule) spi);
        }
      } catch (final ServiceConfigurationError expected) {
        // ignore expected error reading the extensions not compiled yet
      }
    }
    return modules;
  }

  static List<InjectPlugin> loadPlugins(ClassLoader classLoader) {
    List<InjectPlugin> plugins = new ArrayList<>();
    ServiceLoader.load(Plugin.class, classLoader).forEach(plugins::add);
    
    final var iterator = ServiceLoader.load(InjectExtension.class, classLoader).iterator();
    while (iterator.hasNext()) {
      try {
        final var spi = iterator.next();
        if (spi instanceof InjectPlugin) {
          plugins.add((InjectPlugin) spi);
        }
      } catch (final ServiceConfigurationError expected) {
        // ignore expected error reading  the extensions not compiled yet
      }
    }
    return plugins;
  }
}
