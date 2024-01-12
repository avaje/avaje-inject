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
  private final Set<UType> genericTypes = new LinkedHashSet<>();

  TypeAppender(ImportTypeMap importTypes) {
    this.importTypes = importTypes;
  }

  void add(UType type) {
    if (type.isGeneric()) {
      addUType(type);
    } else {
      addSimpleType(type.mainType());
    }
  }

  void add(List<UType> sourceTypes) {
    sourceTypes.forEach(this::add);
  }

  void addSimpleType(String classType) {
    String shortName = importTypes.add(classType);
    types.add(shortName + ".class");
  }

  private void addUType(UType genericType) {
    genericTypes.add(genericType);
    types.add("TYPE_" + Util.shortName(genericType).replace(".", "_"));
  }

  Set<UType> genericTypes() {
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
