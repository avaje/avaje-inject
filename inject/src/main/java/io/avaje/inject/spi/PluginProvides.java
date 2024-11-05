package io.avaje.inject.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers {@link InjectPlugin} classes for auto-detection.
 * <p>
 * Plugins can be registered traditionally via service loading etc but
 * if we use this {@code @PluginProvides} annotation, then avaje inject
 * can ALSO auto-detect the plugin and the types that it provides.
 * Otherwise, we need to use Maven/Gradle plugins to perform this detection.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PluginProvides {

  /**
   * The types this plugin provides.
   */
  Class<?>[] provides() default {};

  /**
   * Fully Qualified Strings of the classes provided. Use when providing generic types
   */
  String[] providesStrings() default {};

  /**
   * The aspects this component provides.
   */
  Class<?>[] providesAspects() default {};
}
