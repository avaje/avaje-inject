package io.avaje.inject;

final class DSystemProps implements io.avaje.inject.spi.PropertyRequiresPlugin {

  @Override
  public boolean contains(String property) {
    return System.getProperty(property) != null || System.getenv(property) != null;
  }

  @Override
  public boolean missing(String property) {
    return System.getProperty(property) == null && System.getenv(property) == null;
  }

  @Override
  public boolean equalTo(String property, String value) {
    return value.equals(System.getProperty(property)) || value.equals(System.getenv(property));
  }

  @Override
  public boolean notEqualTo(String property, String value) {
    return !value.equals(System.getProperty(property)) && !value.equals(System.getenv(property));
  }
}
