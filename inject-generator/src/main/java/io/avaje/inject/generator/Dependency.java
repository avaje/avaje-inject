package io.avaje.inject.generator;

final class Dependency {

  private final String name;
  private final boolean softDependency;

  Dependency(String name) {
    if (name.startsWith("soft:")) {
      this.softDependency = true;
      this.name = name.substring(5);
    } else {
      this.softDependency = false;
      this.name = name;
    }
  }

  Dependency(String name, boolean softDependency) {
    this.name = name;
    this.softDependency = softDependency;
  }

  @Override
  public String toString() {
    return softDependency ? "soft:" + name : name;
  }

  String getName() {
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
}
