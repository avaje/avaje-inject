package io.avaje.inject.spi;

import io.avaje.inject.BeanScopeBuilder;

import java.lang.reflect.Type;

/**
 * A Plugin that can be applied when creating a bean scope.
 * <p>
 * Typically, a plugin might provide a default dependency via {@link BeanScopeBuilder#provideDefault(Type, java.util.function.Supplier)}.
 */
public interface Plugin {

  /**
   * Return the classes that the plugin provides.
   */
  Class<?>[] provides();

  /**
   * Apply the plugin to the scope builder.
   */
  void apply(BeanScopeBuilder builder);
}
