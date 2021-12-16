package io.avaje.inject.generator;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class BeanAspects {

  static final BeanAspects EMPTY = new BeanAspects();

  private final List<AspectMethod> aspectMethods;
  private final Set<String> targets;

  private BeanAspects() {
    this.aspectMethods = Collections.emptyList();
    this.targets = Collections.emptySet();
  }

  BeanAspects(List<AspectMethod> aspectMethods) {
    this.aspectMethods = aspectMethods;
    this.targets = initTargets();
  }

  boolean hasAspects() {
    return !aspectMethods.isEmpty();
  }

  Set<String> targets() {
    return targets;
  }

  void extraImports(Set<String> importTypes) {
    for (AspectMethod aspectMethod : aspectMethods) {
      aspectMethod.addImports(importTypes);
    }
  }

  Set<String> initTargets() {
    Set<String> targets = new LinkedHashSet<>();
    for (AspectMethod aspectMethod : aspectMethods) {
      aspectMethod.addTargets(targets);
    }
    return targets;
  }

  void writeFields(Append writer) {
    for (String target : targets) {
      String type = Util.shortName(target);
      String name = Util.initLower(type);
      writer.append("  private final %s %s;", type, name).eol();
    }
    writer.eol();
  }

  List<AspectMethod> methods() {
    return aspectMethods;
  }

}
