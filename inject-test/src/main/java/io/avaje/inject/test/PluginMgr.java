package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.lang.Nullable;

import java.util.ServiceLoader;

final class PluginMgr {

  private PluginMgr() {}

  private static final Plugin plugin = init();

  private static Plugin init() {
    return ServiceLoader.load(Plugin.class).findFirst().orElse(null);
  }

  @Nullable
  static Plugin plugin() {
    return plugin;
  }

  /**
   * Return a new plugin scope (if there is a plugin).
   */
   @Nullable
  static Plugin.Scope scope(BeanScope beanScope) {
    return plugin == null ? null : plugin.createScope(beanScope);
  }
}
