package io.avaje.inject.spi;

import java.util.EnumMap;
import java.util.Map;

final class EnumUtil {

  /**
   * Assumes that qualifier only has 1 member and that is an enum of the specified type.
   */
  @SuppressWarnings("unchecked")
  static <E extends Enum<E>, T> EnumMap<E, T> toEnumMap(Class<E> enumType, Map<String, Object> qualifierMap) {
    final var asEnumMap = new EnumMap<>(enumType);
    for (var entry : qualifierMap.entrySet()) {
      String rawQualifier = entry.getKey();
      int open = rawQualifier.indexOf("(value=");
      int close = rawQualifier.lastIndexOf(')');
      String rawEnumKey = rawQualifier.substring(open + 7, close);
      E key = Enum.valueOf(enumType, rawEnumKey);
      asEnumMap.put(key, entry.getValue());
    }
    return (EnumMap<E, T>) asEnumMap;
  }
}
