package io.avaje.inject.generator;

final class Dependency {

  private final String name;
  private final boolean softDependency;

  Dependency(String name) {
    this(name, false);
  }

  Dependency(String name, boolean softDependency) {
    this.name = name;
    this.softDependency = softDependency;
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
}
