package io.avaje.inject.generator;

final class UtilType {

  private enum Type {
    LIST,
    SET,
    MAP,
    OPTIONAL,
    PROVIDER,
    OTHER
  }

  private final Type type;
  private final String rawType;

  private UtilType(Type type, String rawType) {
    this.type = type;
    this.rawType = rawType;
  }

  static UtilType of(String rawType) {
    if (rawType.startsWith("java.util.List<")) {
      return new UtilType(Type.LIST, rawType);
    } else if (rawType.startsWith("java.util.Set<")) {
      return new UtilType(Type.SET, rawType);
    } else if (rawType.startsWith("java.util.Map<java.lang.String,")) {
      return new UtilType(Type.MAP, rawType);
    } else if (rawType.startsWith("java.util.Optional<")) {
      return new UtilType(Type.OPTIONAL, rawType);
    } else if (Util.isProvider(rawType)) {
      return new UtilType(Type.PROVIDER, rawType);
    } else {
      return new UtilType(Type.OTHER, rawType);
    }
  }

  /**
   * Only use implied qualifier name with getOptional() and get().
   */
  boolean allowsNamedQualifier() {
    return type == Type.OPTIONAL || type == Type.OTHER;
  }

  boolean isCollection() {
    return type == Type.LIST || type == Type.SET;
  }

  String full() {
    return rawType;
  }

  String rawType(boolean beanMap) {
    switch (type) {
      case SET:
        return Util.extractSet(rawType);
      case LIST:
        return Util.extractList(rawType);
      case MAP:
        if (beanMap) {
          var listType = Util.extractMap(rawType);
          if (!listType.startsWith("java.util.List<")) {
            throw new IllegalStateException(
                "Qualified Maps must be in the form Map<String, List<T>>");
          }
          return Util.extractList(listType);
        }
        return rawType;
      case OPTIONAL:
        return Util.extractOptionalType(rawType);
      default:
        return rawType;
    }
  }

  String getMethod(boolean nullable, boolean beanMap) {
    switch (type) {
      case SET:
        return "set(";
      case LIST:
        return "list(";
      case MAP:
        if (beanMap) {
          return "map(";
        }
        break;
      case OPTIONAL:
        return "getOptional(";
      case PROVIDER:
        return "getProvider(";
    }
    return nullable ? "getNullable(" : "get(";
  }

}
