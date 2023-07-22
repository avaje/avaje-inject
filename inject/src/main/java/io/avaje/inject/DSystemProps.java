package io.avaje.inject;

import java.util.Optional;

final class DSystemProps implements io.avaje.inject.spi.PropertyRequiresPlugin {

  @Override
  public Optional<String> get(String property) {
    return Optional.ofNullable(System.getProperty(property))
      .or(() -> Optional.ofNullable(System.getenv(property)));
  }

  @Override
  public boolean contains(String property) {
    return System.getProperty(property) != null || System.getenv(property) != null;
  }

  @Override
  public boolean equalTo(String property, String value) {
    return value.equals(System.getProperty(property)) || value.equals(System.getenv(property));
  }

  @Override
  public void set(String property, String value) {
    System.setProperty(property, value);
  }
}
