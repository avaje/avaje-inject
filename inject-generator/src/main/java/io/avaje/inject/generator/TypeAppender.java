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
  private final Set<GenericType> genericTypes = new LinkedHashSet<>();

  TypeAppender(Set<String> importTypes) {
    this.importTypes = importTypes;
  }

  void add(String classType) {
    importTypes.add(classType);
    types.add(Util.shortName(classType) + ".class");
  }

  void add(List<String> types) {
    for (String type : types) {
      if (GenericType.isGeneric(type)) {
        genericTypes.add(GenericType.parse(type));
      } else {
        add(type);
      }
    }
  }

  Set<GenericType> genericTypes() {
    return genericTypes;
  }

  String asString() {
    if (!genericTypes.isEmpty()) {
      for (GenericType genericType : genericTypes) {
        types.add("TYPE_" + genericType.shortName());
      }
    }

    int count = 0;
    final StringBuilder sb = new StringBuilder();
    for (String type : types) {
      if (count++ > 0) {
        sb.append(", ");
      }
      sb.append(type);
    }
    return sb.toString();
  }
}
