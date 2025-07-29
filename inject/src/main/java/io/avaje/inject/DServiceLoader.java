package io.avaje.inject;

import io.avaje.inject.spi.*;
import org.jspecify.annotations.Nullable;

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
  private @Nullable ModuleOrdering moduleOrdering;
  private @Nullable ConfigPropertyPlugin propertyPlugin;

  DServiceLoader(@Nullable ClassLoader classLoader) {
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
