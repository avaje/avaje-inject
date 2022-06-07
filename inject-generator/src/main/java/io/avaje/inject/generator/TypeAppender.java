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

  void add(GenericType type) {
    if (type.isGenericType()) {
      addGenericType(type);
    } else {
      addSimpleType(type.topType());
    }
  }

  void add(List<String> sourceTypes) {
    for (String type : sourceTypes) {
      if (GenericType.isGeneric(type)) {
        addGenericType(GenericType.parse(type));
      } else {
        addSimpleType(type);
      }
    }
  }

  void addSimpleType(String classType) {
    importTypes.add(classType);
    types.add(Util.shortName(classType) + ".class");
  }

  private void addGenericType(GenericType genericType) {
    genericTypes.add(genericType);
    types.add("TYPE_" + genericType.shortName());
  }

  Set<GenericType> genericTypes() {
    return genericTypes;
  }

  String asString() {
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
