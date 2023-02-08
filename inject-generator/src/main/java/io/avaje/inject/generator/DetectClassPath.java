package io.avaje.inject.generator;

/**
 * Detect if the annotation processor has access to the classpath.
 * <p>
 * If so it can load externally provided components and plugins without
 * needing to use the inject-maven-plugin.
 */
final class DetectClassPath {

  private static final boolean hasClassPathAccess = init();

  private static boolean init() {
    try {
      Class.forName(Constants.MODULE);
      return true;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  static boolean hasClassPathAccess() {
    return  hasClassPathAccess;
  }
}
