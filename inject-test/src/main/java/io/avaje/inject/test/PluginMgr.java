package io.avaje.inject.test;

import java.util.ServiceLoader;

import org.jspecify.annotations.Nullable;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.Plugin.Scope;

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
  static Scope scope(BeanScope beanScope) {
    return plugin == null ? null : plugin.createScope(beanScope);
  }
}
