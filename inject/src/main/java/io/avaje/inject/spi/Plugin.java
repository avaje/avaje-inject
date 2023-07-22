package io.avaje.inject.spi;

import io.avaje.inject.BeanScopeBuilder;

import java.lang.reflect.Type;

/**
 * A Plugin that can be applied when creating a bean scope.
 *
 * <p>Typically, a plugin might provide a default dependency via {@link
 * BeanScopeBuilder#provideDefault(Type, java.util.function.Supplier)}.
 */
public interface Plugin {

  /**
   * Empty array of classes.
   */
  Class<?>[] EMPTY_CLASSES = {};

  /**
   * Apply the plugin to the scope builder.
   */
  void apply(BeanScopeBuilder builder);

  /**
   * Return the classes that the plugin provides.
   */
  default Class<?>[] provides() {
    return EMPTY_CLASSES;
  }

  /**
   * Return the aspect classes that the plugin provides.
   */
  default Class<?>[] providesAspects() {
    return EMPTY_CLASSES;
  }
}
