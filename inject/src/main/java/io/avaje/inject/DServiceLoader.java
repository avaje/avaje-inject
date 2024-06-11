package io.avaje.inject;

import io.avaje.inject.spi.*;
import io.avaje.inject.spi.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Load all the services using the common service interface.
 */
final class DServiceLoader {

  private final List<InjectPlugin> plugins = new ArrayList<>();
  private final List<AvajeModule> modules = new ArrayList<>();
  private ModuleOrdering moduleOrdering;
  private ConfigPropertyPlugin propertyPlugin;

  DServiceLoader(ClassLoader classLoader) {
    for (var spi : ServiceLoader.load(InjectExtension.class, classLoader)) {
      if (spi instanceof InjectPlugin) {
        plugins.add((InjectPlugin) spi);
      } else if (spi instanceof AvajeModule) {
        modules.add((AvajeModule) spi);
      } else if (spi instanceof ModuleOrdering) {
        moduleOrdering = (ModuleOrdering) spi;
      } else if (spi instanceof ConfigPropertyPlugin) {
        propertyPlugin = (ConfigPropertyPlugin) spi;
      }
    }
    // older plugins and modules
    ServiceLoader.load(Plugin.class, classLoader).forEach(plugins::add);
    ServiceLoader.load(Module.class, classLoader).forEach(modules::add);
  }

  List<InjectPlugin> plugins() {
    return plugins;
  }

  List<AvajeModule> modules() {
    return modules;
  }

  Optional<ModuleOrdering> moduleOrdering() {
    return Optional.ofNullable(moduleOrdering);
  }

  Optional<ConfigPropertyPlugin> propertyPlugin() {
    return Optional.ofNullable(propertyPlugin);
  }
}
