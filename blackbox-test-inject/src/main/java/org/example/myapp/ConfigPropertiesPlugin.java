package org.example.myapp;

import java.util.Optional;

import org.other.one.OtherComponent2;

import io.avaje.config.Config;
import io.avaje.inject.Component;
import io.avaje.inject.spi.PropertyRequiresPlugin;

@Component.Import(value = OtherComponent2.class, packagePrivate = false)
public class ConfigPropertiesPlugin implements PropertyRequiresPlugin {

  @Override
  public Optional<String> get(String property) {
    return Config.getOptional(property);
  }

  @Override
  public boolean contains(String property) {
    return Config.getNullable(property) != null;
  }

  @Override
  public boolean missing(String property) {
    return Config.getNullable(property) == null;
  }

  @Override
  public boolean equalTo(String property, String value) {
    return value.equals(Config.getNullable(property));
  }

  @Override
  public boolean notEqualTo(String property, String value) {
    return !value.equals(Config.getNullable(property));
  }
}
