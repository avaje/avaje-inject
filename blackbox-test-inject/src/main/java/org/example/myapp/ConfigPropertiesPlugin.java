package org.example.myapp;

import io.avaje.config.Config;
import io.avaje.inject.spi.PropertyRequiresPlugin;


public class ConfigPropertiesPlugin implements PropertyRequiresPlugin {
  @Override
  public boolean contains(String property) {
    return Config.getOptional(property).isPresent();
  }

  @Override
  public boolean missing(String property) {
    return Config.getOptional(property).isEmpty();
  }

  @Override
  public boolean equalTo(String property, String value) {
    return Config.getOptional(property).filter(value::equals).isPresent();
  }

  @Override
  public boolean notEqualTo(String property, String value) {
    return Config.getOptional(property).filter(value::equals).isEmpty();
  }
}
