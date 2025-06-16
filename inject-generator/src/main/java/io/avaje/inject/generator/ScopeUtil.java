package io.avaje.inject.generator;

final class ScopeUtil {

  static String initName(String name) {
    name = name(name);
    if (name == null) {
      return null;
    }
    switch (name) {
      case "Inject":
        return "DInject";
      case "Avaje":
        return "AvajeInject";
      default:
        return name;
    }
  }

  static String name(String name) {
    if (name == null) {
      return null;
    }
    final int pos = name.lastIndexOf('.');
    if (pos > -1) {
      name = name.substring(pos + 1);
    }
    if (name.endsWith("Scope")) {
      name = name.substring(0, name.length() - 5);
    }
    if (name.endsWith("Module")) {
      name = name.substring(0, name.length() - 6);
    }
    return camelCase(name);
  }

  private static String camelCase(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean upper = true;
    for (char aChar : name.toCharArray()) {
      if (Character.isLetterOrDigit(aChar)) {
        if (upper) {
          aChar = Character.toUpperCase(aChar);
          upper = false;
        }
        sb.append(aChar);
      } else if (toUpperOn(aChar)) {
        upper = true;
      }
    }
    return sb.toString();
  }

  private static boolean toUpperOn(char aChar) {
    return aChar == ' ' || aChar == '-' || aChar == '_';
  }
}
