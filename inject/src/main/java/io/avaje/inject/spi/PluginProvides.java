package io.avaje.inject.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Hold bean dependency metadata for compile time InjectPlugin auto detection. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PluginProvides {

  /** The types this component provides. */
  Class<?>[] provides() default {};

  /** Fully Qualified Strings of the classes provided. Use when the plugin provides generic types */
  String[] providesStrings() default {};

  /** The aspects this component provides. */
  Class<?>[] providesAspects() default {};
}
