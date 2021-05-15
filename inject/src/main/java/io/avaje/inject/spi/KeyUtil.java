package io.avaje.inject.spi;

/**
 * Build a "flat key" combining type and name.
 */
class KeyUtil {

  static String key(Class<?> type, String name) {
    return name == null ? type.getCanonicalName() : type.getCanonicalName() + "|" + name;
  }

}
