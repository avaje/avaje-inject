package io.avaje.inject.generator;

import java.util.List;
import java.util.Set;

/**
 * Helper for building the registration types.
 */
class TypeAppender {

  private final Set<String> importTypes;
  private int count;
  private StringBuilder sb = new StringBuilder();

  TypeAppender(Set<String> importTypes) {
    this.importTypes = importTypes;
  }

  void add(String type) {
    addType(type);
  }

  void add(List<String> types) {
    for (String type : types) {
      if (!GenericType.isGeneric(type)) {
        if (count > 0) {
          sb.append(", ");
        }
        addType(type);
      }
    }
  }

  private void addType(String type) {
    count++;
    importTypes.add(type);
    sb.append(Util.shortName(type)).append(".class");
  }

  String asString() {
    return sb.toString();
  }
}
