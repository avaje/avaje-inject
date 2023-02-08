package io.avaje.inject.generator;

import io.avaje.inject.spi.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

final class ExternalPluginLoad {

  /**
   * Return the components that plugins in the classpath provide.
   */
  static List<String> load() {
    List<String> pluginProvides = new ArrayList<>();
    try {
      for (final Plugin plugin : ServiceLoader.load(Plugin.class, Processor.class.getClassLoader())) {
        for (final Class<?> provide : plugin.provides()) {
          pluginProvides.add(provide.getCanonicalName());
        }
      }
    } catch (Throwable e) {
      // ignore
    }
    return pluginProvides;
  }
}
