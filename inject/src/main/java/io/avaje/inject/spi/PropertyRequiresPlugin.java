package io.avaje.inject.spi;

public interface PropertyRequiresPlugin {

  boolean contains(String property);

  boolean missing(String property);

  boolean equalTo(String property, String value);

  boolean notEqualTo(String property, String value);
}
