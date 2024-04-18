package io.avaje.inject.generator;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

final class AvajeModule {

  private final String fqn;
  private final List<String> provides;
  private final List<String> requires;

  AvajeModule(String name, List<String> provides, List<String> requires) {
    this.fqn = name;
    this.provides = provides;
    this.requires = requires;
  }

  AvajeModule(String[] moduleCsv) {
    this.fqn = moduleCsv[0];
    this.provides = Arrays.stream(moduleCsv[1].split(",")).filter(not(String::isBlank)).collect(toList());
    this.requires = Arrays.stream(moduleCsv[2].split(",")).filter(not(String::isBlank)).collect(toList());
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
    return "AvajeModule [fqn=" + fqn + ", provides=" + provides + ", requires=" + requires + "]";
  }
}
