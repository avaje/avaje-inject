package io.avaje.inject.spi;

import java.lang.reflect.Type;

/**
 * Build a "flat key" combining type and name.
 */
final class KeyUtil {

  static String lower(String name) {
    return name == null ? null : name;
  }

  static String key(Type type, String name) {
    return name == null ? type.getTypeName() : type.getTypeName() + "|" + name;
  }

}
