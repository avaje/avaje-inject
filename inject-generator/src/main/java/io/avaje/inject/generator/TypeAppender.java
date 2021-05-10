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

  boolean isEmpty() {
    return count == 0;
  }

  void add(List<String> types) {
    for (String type : types) {
      if (!GenericType.isGeneric(type)) {
        importTypes.add(type);
        if (count++ > 0) {
          sb.append(", ");
        }
        sb.append(Util.shortName(type)).append(".class");
      }
    }
  }

  String asString() {
    return sb.toString();
  }
}
