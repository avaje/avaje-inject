package io.avaje.inject.generator;

import io.avaje.inject.Factory;
import io.avaje.inject.Primary;
import io.avaje.inject.Secondary;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

class IncludeAnnotations {

  /**
   * Annotations that we don't bother registering lists for.
   */
  private static final Set<String> EXCLUDED_ANNOTATIONS = new HashSet<>();

  static {
    EXCLUDED_ANNOTATIONS.add("javax.annotation.Generated");
    EXCLUDED_ANNOTATIONS.add(Singleton.class.getName());
    EXCLUDED_ANNOTATIONS.add(Named.class.getName());
    EXCLUDED_ANNOTATIONS.add(Factory.class.getName());
    EXCLUDED_ANNOTATIONS.add(Primary.class.getName());
    EXCLUDED_ANNOTATIONS.add(Secondary.class.getName());
    EXCLUDED_ANNOTATIONS.add(Constants.KOTLIN_METADATA);
    EXCLUDED_ANNOTATIONS.add(Constants.PATH);
    EXCLUDED_ANNOTATIONS.add(Constants.GENERATED_9);
  }

  /**
   * Return true if the annotation should be included.
   */
  static boolean include(String annotationType) {
    if (annotationType.startsWith("lombok.")) {
      return false;
    }
    return !EXCLUDED_ANNOTATIONS.contains(annotationType);
  }
}
