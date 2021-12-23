package io.avaje.inject.generator;

public class Dependency {
  private final String name;
  private final boolean softDependency;

  public Dependency(String name) {
    this(name, false);
  }

  public Dependency(String name, boolean softDependency) {
    this.name = name;
    this.softDependency = softDependency;
  }

  public String getName() {
    return name;
  }

  public boolean isSoftDependency() {
    return softDependency;
  }
}
