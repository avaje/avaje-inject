package io.avaje.inject.spi;

import java.lang.reflect.Type;
import java.util.Arrays;

import io.avaje.inject.BeanScopeBuilder;

/**
 * A Plugin that can be applied when creating a bean scope.
 *
 * <p>Typically, a plugin might provide a default dependency via {@link
 * BeanScopeBuilder#provideDefault(Type, java.util.function.Supplier)}.
 */
public interface InjectPlugin extends InjectExtension {

  /** Empty array of classes. */
  Class<?>[] EMPTY_CLASSES = {};

  /** Empty array of classes. */
  String[] EMPTY_STRINGS = {};

  /** Apply the plugin to the scope builder. */
  void apply(BeanScopeBuilder builder);

  /** Return the classes that the plugin provides. */
  default Type[] provides() {
    return EMPTY_CLASSES;
  }

  /** Return the type names of types this module explicitly provides to other modules. */
  default String[] providesBeans() {
    return Arrays.stream(provides()).map(Type::getTypeName).toArray(String[]::new);
  }

  /** Return the aspect classes that the plugin provides. */
  default Class<?>[] providesAspects() {
    return EMPTY_CLASSES;
  }

  /** Return the type names of types this module explicitly provides to other modules. */
  default String[] providesAspectBeans() {
    return Arrays.stream(providesAspects()).map(Type::getTypeName).toArray(String[]::new);
  }
}
