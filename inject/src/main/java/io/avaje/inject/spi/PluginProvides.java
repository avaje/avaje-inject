package io.avaje.inject.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers {@link InjectPlugin} classes for auto-detection with JPMS.
 *
 * <p>Plugins can be registered with the ServiceLoader manually, but manually registered plugins may cause dependency missing
 * errors to consumers using JPMS. (This can be fixed if the consumer uses the inject maven/gradle plugin)
 *
 * <p>If we use this {@code @PluginProvides} annotation, then avaje inject can auto-detect the
 * plugin and the types that it provides when a consumer uses JPMS. This eliminates the need for a plugin consumer to take action.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PluginProvides {

  /**
   * The types this plugin provides.
   */
  Class<?>[] value() default {};

  /**
   * Fully Qualified Strings of the classes provided. Use when providing generic types
   */
  String[] providesStrings() default {};

  /**
   * The aspects this component provides.
   */
  Class<?>[] providesAspects() default {};
}
