package io.avaje.inject.generator;

import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.type.TypeKind;

final class GenericTypeFieldsWriter {

  private GenericTypeFieldsWriter() {}

  static void write(Append writer, Set<UType> types) {
    Set<UType> genericTypes =
        types.stream()
            .map(Util::unwrapProvider)
            .filter(UType::isGeneric)
            .collect(toSet());

    if (genericTypes.isEmpty()) {
      return;
    }

    final Map<String, String> seenShortNames = new HashMap<>();
    final Set<String> writtenFields = new HashSet<>();
    for (final UType utype : genericTypes) {
      var type = Util.unwrapProvider(utype);
      final var fieldName = Util.shortName(type).replace(".", "_");
      final var components = type.componentTypes();
      if (components.size() == 1 && components.get(0).kind() == TypeKind.WILDCARD
          || components.stream().anyMatch(u -> u.kind() == TypeKind.TYPEVAR)
          || !writtenFields.add(fieldName)) {
        continue;
      }

      writer.append("  public static final Type TYPE_%s =", fieldName).eol()
          .append("      new GenericType<");
      writeType(type, seenShortNames, writer);
      writer.append(">(){}.type();").eol();
    }
    writer.eol();
  }

  private static void writeType(UType type, Map<String, String> seenShortNames, Append writer) {
    final var typeShortName = Util.shortName(type.mainType());
    final var mainType = seenShortNames.computeIfAbsent(typeShortName, k -> type.mainType());
    final var shortName = type.mainType().equals(mainType) ? typeShortName : type.mainType();
    if (!type.isGeneric()) {
      writer.append(shortName);
      return;
    }

    writer.append(shortName).append("<");
    boolean first = true;
    for (final var param : type.componentTypes()) {
      if (first) {
        first = false;
      } else {
        writer.append(", ");
      }
      writeType(param, seenShortNames, writer);
    }
    writer.append(">");
  }
}
