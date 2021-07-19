package io.avaje.inject.generator;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ScopeUtil {

  private static final String INJECT_MODULE = "io.avaje.inject.InjectModule";

  static List<String> readProvides(Element element) {
    return readClasses(element, "provides");
  }

  static List<String> readRequires(Element element) {
    return readClasses(element, "requires");
  }

  static List<String> readClasses(Element element, String attributeName) {
    List<String> requiresList = new ArrayList<>();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (INJECT_MODULE.equals(annotationMirror.getAnnotationType().toString())) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
          if (entry.getKey().toString().startsWith(attributeName)) {
            for (Object requiresType : (List<?>) entry.getValue().getValue()) {
              String fullName = requiresType.toString();
              fullName = fullName.substring(0, fullName.length() - 6);
              requiresList.add(fullName);
            }
          }
        }
      }
    }
    return requiresList;
  }

  public static String name(String name) {
    if (name == null) {
      return null;
    }
    final int pos = name.lastIndexOf('.');
    if (pos > -1) {
      name = name.substring(pos + 1);
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
