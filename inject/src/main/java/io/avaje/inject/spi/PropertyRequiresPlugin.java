package io.avaje.inject.spi;

import java.util.Optional;

import io.avaje.lang.NonNullApi;

/**
 * Plugin interface which contains the application properties used for wiring. Used with
 * {@link io.avaje.inject.RequiresProperty} and {@link io.avaje.inject.Profile}.
 *
 * <p>The plugin is loaded via ServiceLoader and defaults to an implementation that uses {@link
 * System#getProperty(String)} and {@link System#getenv(String)}.
 */
@NonNullApi
public interface PropertyRequiresPlugin extends InjectSPI {

  /**
   * Return a configuration value that might not exist.
   */
  Optional<String> get(String property);

  /**
   * Return true if the property is defined.
   */
  boolean contains(String property);

  /** Return true if the property is not defined. */
  default boolean missing(String property) {
    return !contains(property);
  }

  /** Return true if the property is equal to the given value. */
  boolean equalTo(String property, String value);

  /** Return true if the property is not defined or not equal to the given value. */
  default boolean notEqualTo(String property, String value) {
    return !equalTo(property, value);
  }
}
