package io.avaje.inject.generator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper for building the registration types.
 */
class TypeAppender {

  private final Set<String> importTypes;
  private final Set<String> types = new LinkedHashSet<>();

  TypeAppender(Set<String> importTypes) {
    this.importTypes = importTypes;
  }

  void add(String type) {
    types.add(type);
    importTypes.add(type);
  }

  void add(List<String> types) {
    for (String type : types) {
      if (!GenericType.isGeneric(type)) {
        add(type);
      }
    }
  }

  String asString() {
    int count = 0;
    final StringBuilder sb = new StringBuilder();
    for (String type : types) {
      if (count++ > 0) {
        sb.append(", ");
      }
      sb.append(Util.shortName(type)).append(".class");
    }
    return sb.toString();
  }
}
