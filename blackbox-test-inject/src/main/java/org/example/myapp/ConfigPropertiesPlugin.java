package org.example.myapp;

import io.avaje.config.Config;
import io.avaje.inject.Component;
import io.avaje.inject.Component.Import.Kind;
import io.avaje.inject.spi.ConfigPropertyPlugin;

import io.avaje.spi.ServiceProvider;
import org.other.one.OtherComponent2;
import org.other.one.OtherComponent3;

import java.util.Optional;

@ServiceProvider
@Component.Import(value = OtherComponent2.class)
@Component.Import(value = OtherComponent3.class, kind = Kind.LAZY)
public class ConfigPropertiesPlugin implements ConfigPropertyPlugin {

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
