package io.avaje.inject.generator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.type.TypeKind;

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

  void add(UType utype) {
    var type = Util.unwrapProvider(utype);
    if (isAddGenericType(type)) {
      addUType(type);
    } else {
      addSimpleType(type.mainType());
    }
  }

  private static boolean isAddGenericType(UType type) {
    var components = type.componentTypes();
    return type.isGeneric()
        && type.kind() != TypeKind.TYPEVAR
        && (components.size() != 1 || components.get(0).kind() != TypeKind.WILDCARD)
        && components.stream()
            .flatMap(TypeAppender::allcomponentTypes)
            .noneMatch(u -> u.kind() == TypeKind.TYPEVAR || u.kind() == TypeKind.WILDCARD);
  }

  private static Stream<UType> allcomponentTypes(UType u) {

    final var componentTypes = u.componentTypes();

    return componentTypes.isEmpty()
        ? Stream.of(u)
        : Stream.concat(
            Stream.of(u), componentTypes.stream().flatMap(TypeAppender::allcomponentTypes));
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
