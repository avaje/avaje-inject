package io.avaje.inject.generator;

import static io.avaje.inject.generator.Constants.CONDITIONAL_DEPENDENCY;
import static io.avaje.inject.generator.Constants.SOFT_DEPENDENCY;

final class Dependency {

  private final String name;
  private boolean softDependency;
  private final boolean conditionalDependency;

  Dependency(String name) {
    String nameStr;
    if (name.startsWith(SOFT_DEPENDENCY)) {
      this.softDependency = true;
      this.conditionalDependency = false;
      nameStr = ProcessorUtils.trimAnnotations(name.substring(5));
    } else if (name.startsWith(CONDITIONAL_DEPENDENCY)) {
      this.softDependency = true;
      this.conditionalDependency = true;
      nameStr = ProcessorUtils.trimAnnotations(name.substring(4));
    } else {
      this.softDependency = false;
      this.conditionalDependency = false;
      nameStr = ProcessorUtils.trimAnnotations(name);
    }
    this.name = nameStr.replace(", ", ",");
  }

  Dependency(String name, boolean softDependency) {
    this.name = ProcessorUtils.trimAnnotations(name).replace(", ", ",");
    this.softDependency = softDependency;
    this.conditionalDependency = false;
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

  /**
   * Return true if a conditional dependency which can be empty.
   *
   * <p>A conditional dependency isn't absolutely required to wire beans.
   */
  public boolean isConditionalDependency() {
    return conditionalDependency;
  }

  String dependsOn() {
    return toString();
  }

  /** External dependency */
  void markExternal() {
    softDependency = true;
  }
}
