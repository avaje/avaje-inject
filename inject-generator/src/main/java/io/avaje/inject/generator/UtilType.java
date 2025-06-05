package io.avaje.inject.generator;

import javax.lang.model.type.TypeMirror;

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
  private final UType uType;

  private UtilType(Type type, String rawType, UType uType) {
    this.type = type;
    this.rawType = rawType;
    this.uType = uType;
  }

  static UtilType of(boolean beanMap, TypeMirror mirror) {
    var uType = UType.parse(mirror);
    var rawType = uType.fullWithoutAnnotations().replace(" ", "");
    if (rawType.startsWith("java.util.List<")) {
      return new UtilType(Type.LIST, rawType, uType.param0());
    } else if (rawType.startsWith("java.util.Set<")) {
      return new UtilType(Type.SET, rawType, uType.param0());
    } else if (rawType.startsWith("java.util.Map<java.lang.String,")) {
      return new UtilType(Type.MAP, rawType, beanMap ? uType.param1() : uType);
    } else if (rawType.startsWith("java.util.Optional<")) {
      return new UtilType(Type.OPTIONAL, rawType, uType.param0());
    } else if (Util.isProvider(rawType)) {
      return new UtilType(Type.PROVIDER, rawType, uType.param0());
    } else {
      return new UtilType(Type.OTHER, rawType, uType);
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
          return Util.extractMap(rawType);
        }
        return rawType;
      case OPTIONAL:
        return Util.extractOptionalType(rawType);
      default:
        return rawType;
    }
  }

  UType toUType() {
    return uType;
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
      default:
        break;
    }
    return nullable ? "getNullable(" : "get(";
  }

}
