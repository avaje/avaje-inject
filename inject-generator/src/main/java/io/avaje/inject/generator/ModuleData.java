package io.avaje.inject.generator;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
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
    return splitTopLevel(moduleCsv[index]).stream()
      .filter(not(String::isBlank))
      .collect(toList());
  }

  /**
   * Split on commas that are not nested inside generic type arguments or qualifier annotation
   * values, e.g. {@code java.util.Map<java.lang.String, java.lang.Long>}.
   */
  static List<String> splitTopLevel(String value) {
    final List<String> result = new ArrayList<>();
    int depth = 0;
    int start = 0;
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      switch (ch) {
        case '<':
        case '(':
        case '{':
        case '[':
          depth++;
          break;
        case '>':
        case ')':
        case '}':
        case ']':
          depth = Math.max(0, depth - 1);
          break;
        case ',':
          if (depth == 0) {
            result.add(value.substring(start, i));
            start = i + 1;
          }
          break;
        default:
          break;
      }
    }
    result.add(value.substring(start));
    return result;
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
