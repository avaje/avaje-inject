package io.avaje.inject;

import io.avaje.config.Config;
import io.avaje.inject.spi.ConfigPropertyPlugin;

import java.util.Optional;

/**
 * Avaje-Config based implementation of ConfigPropertyPlugin.
 */
final class DConfigProps implements ConfigPropertyPlugin {

  @Override
  public Optional<String> get(String property) {
    return Config.getOptional(property);
  }

  @Override
  public boolean contains(String property) {
    return Config.getNullable(property) != null;
  }

  @Override
  public boolean equalTo(String property, String value) {
    return value.equals(Config.getNullable(property));
  }
}
