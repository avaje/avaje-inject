package io.avaje.inject.generator;

final class Dependency {

  private final String name;
  private boolean softDependency;
  private final boolean conditionalDependency;

  Dependency(String name) {
    if (name.startsWith("soft:")) {
      this.softDependency = true;
      this.conditionalDependency = false;
      this.name = Util.trimAnnotations(name.substring(5));
    } else if (name.startsWith("con:")) {
      this.softDependency = true;
      this.conditionalDependency = true;
      this.name = Util.trimAnnotations(name.substring(4));
    } else {
      this.softDependency = false;
      this.conditionalDependency = false;
      this.name = Util.trimAnnotations(name);
    }
  }

  Dependency(String name, boolean softDependency) {
    this.name = Util.trimAnnotations(name);
    this.softDependency = softDependency;
    this.conditionalDependency = false;
  }

  @Override
  public String toString() {
    return softDependency ? "soft:" + name : name;
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
