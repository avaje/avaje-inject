package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A type with generic parameters and potentially nested.
 */
class GenericType {

  private String mainType;

  private final List<GenericType> params = new ArrayList<>();

  /**
   * Return true if this is a generic type.
   */
  static boolean isGeneric(String raw) {
    return raw.contains("<");
  }

  /**
   * Parse and return as GenericType.
   */
  static GenericType parse(String raw) {
    return new GenericTypeParser(raw).parse();
  }

  /**
   * Parse and return as GenericType or null if it is not generic.
   */
  static GenericType maybe(String paramType) {
    return isGeneric(paramType) ? parse(paramType) : null;
  }


  void addImports(Set<String> importTypes) {
    final String type = trimExtends();
    if (includeInImports(type)) {
      importTypes.add(type);
    }
    for (GenericType param : params) {
      param.addImports(importTypes);
    }
  }

  private boolean includeInImports(String type) {
    return !type.startsWith("java.lang.") && type.contains(".");
  }

  /**
   * Append the short version of the type (given the type and parameters are in imports).
   */
  void writeShort(Append writer) {

    String main = Util.shortName(trimExtends());
    writer.append(main);

    final int paramCount = params.size();
    if (paramCount > 0) {
      writer.append("<");
      for (int i = 0; i < paramCount; i++) {
        if (i > 0) {
          writer.append(",");
        }
        params.get(i).writeShort(writer);
      }
      writer.append(">");
    }
  }

  private String trimExtends() {
    if (mainType.startsWith("? extends ")) {
      return mainType.substring(10);
    }
    return mainType;
  }

  /**
   * Return the main type.
   */
  String getMainType() {
    return mainType;
  }

  /**
   * Return the parameter types.
   */
  List<GenericType> getParams() {
    return params;
  }

  void setMainType(String mainType) {
    this.mainType = mainType;
  }

  void addParam(GenericType param) {
    params.add(param);
  }

}
