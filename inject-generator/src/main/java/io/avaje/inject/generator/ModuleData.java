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

  ModuleData(String name, List<String> provides, List<String> requires) {
    this.fqn = name;
    this.provides = provides;
    this.requires = requires;
  }

  static Optional<ModuleData> of(String[] moduleCsv) {
    try {
      return Optional.of(
          new ModuleData(
              moduleCsv[0],
              Arrays.stream(moduleCsv[1].split(",")).filter(not(String::isBlank)).collect(toList()),
              Arrays.stream(moduleCsv[2].split(","))
                  .filter(not(String::isBlank))
                  .collect(toList())));

    } catch (Exception e) {
      System.err.println("Failed to parse" + Arrays.toString(moduleCsv));
    }
    return Optional.empty();
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

  @Override
  public String toString() {
    return "ModuleData [fqn=" + fqn + ", provides=" + provides + ", requires=" + requires + "]";
  }
}
