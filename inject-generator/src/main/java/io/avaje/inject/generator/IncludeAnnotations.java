package io.avaje.inject.generator;

import java.util.HashSet;
import java.util.Set;

class IncludeAnnotations {

  private static final String[] EXCLUDED_PREFIX = {"javax.annotation.", "javax.inject.", "jakarta.annotation.", "jakarta.inject.", "io.avaje.inject.", "lombok."};
  private static final String[] EXCLUDED_SUFFIX = {".PostConstruct", ".PreDestory"};

  /**
   * Annotations that we don't bother registering lists for.
   */
  private static final Set<String> EXCLUDED_ANNOTATIONS = new HashSet<>();

  static {
    EXCLUDED_ANNOTATIONS.add(Constants.KOTLIN_METADATA);
    EXCLUDED_ANNOTATIONS.add(Constants.PATH);
  }

  /**
   * Return true if the annotation should be included.
   */
  static boolean include(String annotationType) {
    for (String prefix : EXCLUDED_PREFIX) {
      if (annotationType.startsWith(prefix)) {
        return false;
      }
    }
    for (String suffix : EXCLUDED_SUFFIX) {
      if (annotationType.endsWith(suffix)) {
        return false;
      }
    }
    return !EXCLUDED_ANNOTATIONS.contains(annotationType);
  }
}
