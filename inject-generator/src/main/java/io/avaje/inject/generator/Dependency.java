package io.avaje.inject.generator;

import static io.avaje.inject.generator.Constants.CONDITIONAL_DEPENDENCY;
import static io.avaje.inject.generator.Constants.SOFT_DEPENDENCY;

final class Dependency {

  private final String name;
  private boolean softDependency;

  Dependency(String type) {
    final String nameStr;
    if (type.startsWith(SOFT_DEPENDENCY)) {
      this.softDependency = true;
      nameStr = type.substring(5);
    } else if (type.startsWith(CONDITIONAL_DEPENDENCY)) {
      this.softDependency = true;
      nameStr = type.substring(4);
    } else {
      this.softDependency = false;
      nameStr = type;
    }
    this.name = nameStr;
  }

  Dependency(String type, String qualifier) {
    String nameStr;
    if (type.startsWith(SOFT_DEPENDENCY)) {
      this.softDependency = true;
      nameStr = ProcessorUtils.trimAnnotations(type.substring(5));
    } else if (type.startsWith(CONDITIONAL_DEPENDENCY)) {
      this.softDependency = true;
      nameStr = ProcessorUtils.trimAnnotations(type.substring(4));
    } else {
      this.softDependency = false;
      nameStr = ProcessorUtils.trimAnnotations(type);
    }
    this.name = Util.addQualifierSuffixTrim(qualifier, nameStr);
  }

  Dependency(String name, String qualifier, boolean softDependency) {
    this.name = Util.addQualifierSuffixTrim(qualifier, ProcessorUtils.trimAnnotations(name));
    this.softDependency = softDependency;
  }

  @Override
  public String toString() {
    return softDependency ? SOFT_DEPENDENCY + name : name;
  }

  String name() {
    return name;
  }

  /**
   * Return true if a collection dependency which can be empty.
   * <p>
   * A soft dependency isn't absolutely required (inject empty lists, sets).
   */
  boolean isSoftDependency() {
    return softDependency;
  }

  String dependsOn() {
    return toString();
  }

  /**
   * External dependency
   */
  void markExternal() {
    softDependency = true;
  }
}
