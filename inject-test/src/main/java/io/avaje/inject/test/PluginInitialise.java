package io.avaje.inject.test;

import io.avaje.lang.Nullable;

import java.util.ServiceLoader;

final class PluginInitialise {

  private static final Plugin plugin = init();

  private static Plugin init() {
    return ServiceLoader.load(Plugin.class).findFirst().orElse(null);
  }

  @Nullable
  static Plugin plugin() {
    return plugin;
  }
}
