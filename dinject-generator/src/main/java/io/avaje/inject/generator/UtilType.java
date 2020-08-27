package io.avaje.inject.generator;

class UtilType {

  private enum Type {
    LIST,
    SET,
    OPTIONAL,
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
    } else if (rawType.startsWith("java.util.Optional<")) {
      return new UtilType(Type.OPTIONAL, rawType);
    } else {
      return new UtilType(Type.OTHER, rawType);
    }
  }

  String rawType() {
    switch (type) {
      case SET:
        return Util.extractSet(rawType);
      case LIST:
        return Util.extractList(rawType);
      case OPTIONAL:
        return Util.extractOptionalType(rawType);
    }
    return rawType;
  }

  String getMethod() {
    switch (type) {
      case SET:
        return "getSet(";
      case LIST:
        return "getList(";
      case OPTIONAL:
        return "getOptional(";
    }
    return "get(";
  }

}
