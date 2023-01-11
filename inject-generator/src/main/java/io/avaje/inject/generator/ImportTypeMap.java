package io.avaje.inject.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Registers types that we want to import - must have unique short names.
 */
final class ImportTypeMap {

  private final Map<String, String> mapByShortName = new HashMap<>();

  /**
   * Return the full types that will be imported.
   */
  Set<String> forImport() {
    return new TreeSet<>(mapByShortName.values());
  }

  /**
   * Return true if there is a full type matching the given suffix.
   */
  boolean containsShortName(String suffix) {
    return mapByShortName.containsKey(suffix);
  }

  /**
   * Register the full type checking for unique short name and returning the short name to use.
   */
  String add(String fullType) {
    String shortName = Util.shortName(fullType);
    String existingFull = mapByShortName.get(shortName);
    if (existingFull == null) {
      mapByShortName.put(shortName, fullType);
      return shortName;
    } else if (existingFull.equals(fullType)) {
      // already existing
      return shortName;
    } else {
      // must use fully qualified type
      return fullType;
    }
  }

}
