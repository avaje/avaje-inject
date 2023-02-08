package io.avaje.inject.generator;

import java.util.HashSet;
import java.util.Set;

/**
 * The types provided by other modules in the classpath at compile time.
 *
 * <p>When we depend on these types they add to the module autoRequires() classes.
 */
final class ExternalProvider {

  private final Set<String> providedTypes = new HashSet<>();

  void init(Set<String> moduleFileProvided) {
    providedTypes.addAll(moduleFileProvided);
    if (DetectClassPath.hasClassPathAccess()) {
      providedTypes.addAll(ExternalProviderLoad.load());
    }
  }

  /**
   * Return true if this type is provided by another module in the classpath. We will add it to
   * autoRequires().
   */
  boolean provides(String type) {
    return providedTypes.contains(type);
  }
}
