package io.avaje.inject.plugin;

import java.util.ArrayList;
import java.util.List;

final class ModuleData {

  private final String fqn;
  private final List<String> provides = new ArrayList<>();
  private final List<String> requires = new ArrayList<>();
  private final List<String> softRequires = new ArrayList<>();

  ModuleData(String name, List<String> provides, List<String> requires, List<String> softRequires) {
    this.fqn = name;
    this.provides.addAll(provides);
    this.requires.addAll(requires);
    this.softRequires.addAll(softRequires);
  }

  List<String> provides() {
    return provides;
  }

  List<String> requires() {
    return requires;
  }

  List<String> softRequires() {
    return softRequires;
  }

  String name() {
    return fqn;
  }
}
