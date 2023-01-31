package io.avaje.inject.generator;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class BeanAspects {

  static final BeanAspects EMPTY = new BeanAspects();

  private final List<AspectMethod> aspectMethods;
  private final Set<String> aspectNames;

  private BeanAspects() {
    this.aspectMethods = Collections.emptyList();
    this.aspectNames = Collections.emptySet();
  }

  BeanAspects(List<AspectMethod> aspectMethods) {
    this.aspectMethods = aspectMethods;
    this.aspectNames = initAspectNames();
  }

  boolean hasAspects() {
    return !aspectMethods.isEmpty();
  }

  Set<String> aspectNames() {
    return aspectNames;
  }

  void extraImports(ImportTypeMap importTypes) {
    for (final AspectMethod aspectMethod : aspectMethods) {
      aspectMethod.addImports(importTypes);
    }
  }

  Set<String> initAspectNames() {
    final Set<String> targets = new LinkedHashSet<>();
    for (final AspectMethod aspectMethod : aspectMethods) {
      aspectMethod.addTargets(targets);
    }
    return targets;
  }

  void writeFields(Append writer) {
    for (final String aspectName : aspectNames) {
      final var type = "AspectProvider<" + aspectName + ">";
      final var name = Util.initLower(aspectName);
      writer.append("  private final %s %s;", type, name).eol();
    }
    writer.eol();
  }

  List<AspectMethod> methods() {
    return aspectMethods;
  }
}
