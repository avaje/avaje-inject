package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds extra PreDestroy methods for a factory.
 * <p>
 * These methods are expected to relate back to a {@code @Bean} method
 * on the same factory.
 */
final class DestroyMethods {

  private final Map<String, DestroyMethod> methods = new HashMap<>();
  private final Set<String> matchedTypes = new HashSet<>();

  void add(ExecutableElement element) {
    Integer priority = PreDestroyPrism.getOptionalOn(element)
      .map(PreDestroyPrism::priority)
      .orElse(null);

    var destroyMethod = new DestroyMethod(element, priority);
    methods.put(destroyMethod.matchType, destroyMethod);
  }

  DestroyMethod match(String returnTypeRaw) {
    var match = methods.get(returnTypeRaw);
    if (match != null) {
      matchedTypes.add(returnTypeRaw);
    }
    return match;
  }

  /**
   * Return PreDestroy methods that were not matched to a {@code @Bean} method
   * on the same factory.
   */
  List<DestroyMethod> unmatched() {
    return methods.values()
      .stream()
      .filter(entry -> !matchedTypes.contains(entry.matchType()))
      .collect(Collectors.toList());
  }

  static final class DestroyMethod {

    private final String method;
    private final Integer priority;
    private final String matchType;
    private final ExecutableElement element;

    DestroyMethod(ExecutableElement element, Integer priority) {
      this.element = element;
      this.method = element.getSimpleName().toString();
      this.matchType = element.getParameters().get(0).asType().toString();
      this.priority = priority;
    }

    String method() {
      return method;
    }

    Integer priority() {
      return priority;
    }

    String matchType() {
      return matchType;
    }

    Element element() {
      return element;
    }
  }
}
