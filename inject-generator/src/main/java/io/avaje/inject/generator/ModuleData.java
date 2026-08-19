package io.avaje.inject.generator;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

final class ModuleData {

  private final String fqn;
  private final List<String> provides;
  private final List<String> requires;
  private final List<String> softRequires;

  ModuleData(String name, List<String> provides, List<String> requires) {
    this(name, provides, requires, List.of());
  }

  ModuleData(String name, List<String> provides, List<String> requires, List<String> softRequires) {
    this.fqn = name;
    this.provides = provides;
    this.requires = requires;
    this.softRequires = softRequires;
  }

  static Optional<ModuleData> of(String[] moduleCsv) {
    try {
      return Optional.of(
        new ModuleData(
          moduleCsv[0],
          split(moduleCsv, 1),
          split(moduleCsv, 2),
          // older csv files (written by a prior plugin version) have no soft requires column
          split(moduleCsv, 3)));

    } catch (Exception e) {
      System.err.println("Failed to parse" + Arrays.toString(moduleCsv));
    }
    return Optional.empty();
  }

  private static List<String> split(String[] moduleCsv, int index) {
    if (index >= moduleCsv.length) {
      return List.of();
    }
    return Arrays.stream(moduleCsv[index].split(","))
      .filter(not(String::isBlank))
      .collect(toList());
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

  @Override
  public String toString() {
    return "ModuleData [fqn=" + fqn + ", provides=" + provides + ", requires=" + requires
      + ", softRequires=" + softRequires + "]";
  }
}
