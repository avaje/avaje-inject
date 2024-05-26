package io.avaje.inject.plugin;

import java.util.ArrayList;
import java.util.List;

final class AvajeModuleData {

  private final String fqn;
  private final List<String> provides = new ArrayList<>();
  private final List<String> requires = new ArrayList<>();

  AvajeModuleData(String name, List<String> provides, List<String> requires) {
    this.fqn = name;
    this.provides.addAll(provides);
    this.requires.addAll(requires);
  }

  List<String> provides() {
    return provides;
  }

  List<String> requires() {
    return requires;
  }

  String name() {
    return fqn;
  }
}
