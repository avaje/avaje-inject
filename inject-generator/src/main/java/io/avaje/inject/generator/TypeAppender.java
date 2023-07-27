package io.avaje.inject.generator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper for building the registration types.
 */
final class TypeAppender {

  private final ImportTypeMap importTypes;
  private final Set<String> types = new LinkedHashSet<>();
  private final Set<GenericType> genericTypes = new LinkedHashSet<>();

  TypeAppender(ImportTypeMap importTypes) {
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
      GenericType genericType = GenericType.parse(type);
      if (genericType.isGenericType()) {
        addGenericType(genericType);
      } else {
        addSimpleType(genericType.topType());
      }
    }
  }

  void addSimpleType(String classType) {
    String shortName = importTypes.add(classType);
    types.add(shortName + ".class");
  }

  private void addGenericType(GenericType genericType) {
    genericTypes.add(genericType);
    types.add("TYPE_" + genericType.shortName().replace(".", "_"));
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
