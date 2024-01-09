package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A type with generic parameters and potentially nested.
 */
final class GenericType {

  /**
   * Trim off generic wildcard from the raw type if present.
   */
  static String trimWildcard(String rawType) {
    if (rawType.endsWith("<?>")) {
      return rawType.substring(0, rawType.length() - 3);
    } else {
      return trimGenericParams(rawType);
    }
  }

  /**
   * Trim off generic type parameters.
   */
  static String trimGenericParams(String rawType) {
    int start = rawType.indexOf('<');
    // no package for any generic parameter types
    if (start > 0 && rawType.indexOf('.', start) == -1 && rawType.lastIndexOf('>') > -1) {
      return rawType.substring(0, start);
    }
    return rawType;
  }

  private final String raw;
  private String mainType;

  private final List<GenericType> params = new ArrayList<>();

  /**
   * Create for top level type.
   */
  GenericType(String raw) {
    this.raw = raw;
  }

  /**
   * Create for parameter type.
   */
  GenericType() {
    this.raw = null;
  }

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
    raw = Util.trimAnnotations(raw);
    raw = trimWildcard(raw);
    if (raw.indexOf('<') == -1) {
      return new GenericType(raw);
    }
    return new GenericTypeParser(raw).parse();
  }

  /**
   * Parse and return the main type if it contains a type parameter like {@code <T>}.
   */
  static String removeParameter(String raw) {
    final GenericType type = parse(raw);
    return type.topType();
  }


  @Override
  public String toString() {
    return raw != null ? raw : mainType + '<' + params + '>';
  }

  boolean isGenericType() {
    return !params.isEmpty();
  }

  boolean isProviderType() {
    return raw.startsWith(Util.PROVIDER_PREFIX);
  }


  void addImports(ImportTypeMap importTypes) {
    final String type = trimExtends();
    if (includeInImports(type)) {
      importTypes.add(type);
    }
    for (GenericType param : params) {
      param.addImports(importTypes);
    }
  }

  private static boolean includeInImports(String type) {
    return type != null
        && type.contains(".")
        && Util.notJavaLang(type);
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

  String shortName() {
    StringBuilder sb = new StringBuilder();
    shortName(sb);
    return sb.toString();
  }

  void shortName(StringBuilder sb) {
    sb.append(Util.shortName(trimExtends()));
    for (GenericType param : params) {
      param.shortName(sb);
    }
  }

  private String trimExtends() {
    String type = topType();
    if (type != null && type.startsWith("? extends ")) {
      return type.substring(10);
    } else if ("?".equals(type)) {
      return "Wildcard";
    }
    return type;
  }

  String topType() {
    return (mainType != null) ? mainType : raw;
  }

  /**
   * Return the main type.
   */
  String mainType() {
    return mainType;
  }

  /**
   * Return the parameter types.
   */
  List<GenericType> params() {
    return params;
  }

  void setMainType(String mainType) {
    this.mainType = mainType;
  }

  void addParam(GenericType param) {
    params.add(param);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GenericType that = (GenericType) o;
    return raw.equals(that.raw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(raw);
  }
}
