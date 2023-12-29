package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.List;

final class AvajeModule {

  private final String fqn;
  private final List<String> provides = new ArrayList<>();
  private final List<String> requires = new ArrayList<>();

  public AvajeModule(String name, List<String> provides, List<String> requires) {
    this.fqn = name;
    this.provides.addAll(provides);
    this.requires.addAll(requires);
  }

  public List<String> provides() {
    return provides;
  }

  public List<String> requires() {
    return requires;
  }

  public String name() {
    return fqn;
  }
}
