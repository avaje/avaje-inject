package io.dinject.generator;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

class Util {

  static boolean isVoid(String type) {
    return "void".equalsIgnoreCase(type);
  }

  static boolean notVoid(String type) {
    return !isVoid(type);
  }

  static String classOfMethod(String method) {
    return packageOf(method);
  }

  static String shortMethod(String method) {
    int p = method.lastIndexOf('.');
    if (p > -1) {
      p = method.lastIndexOf('.', p - 1);
      if (p > -1) {
        return method.substring(p + 1);
      }
    }
    return method;
  }

  static String packageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String unwrapProvider(String maybeProvider) {
    if (isProvider(maybeProvider)) {
      return extractProviderType(maybeProvider);
    } else {
      return maybeProvider;
    }
  }

  static String shortName(String fullType) {
    int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else {
      return fullType.substring(p + 1);
    }
  }

  static String extractOptionalType(String rawType) {
    return rawType.substring(19, rawType.length() - 1);
  }

  static boolean isOptional(String rawType) {
    return rawType.startsWith("java.util.Optional<");
  }

  static String extractList(String rawType) {
    String listType = rawType.substring(15, rawType.length() - 1);
    if (listType.startsWith("? extends")) {
      return listType.substring(10);
    }
    return listType;
  }

  static boolean isList(String rawType) {
    return rawType.startsWith("java.util.List<");
  }

  private static boolean isProvider(String rawType) {
    return rawType.startsWith("javax.inject.Provider<");
  }

  private static String extractProviderType(String rawType) {
    return rawType.substring(22, rawType.length() - 1);
  }

  /**
   * Return the common parent package.
   */
  static String commonParent(String currentTop, String aPackage) {

    if (aPackage == null) return currentTop;
    if (currentTop == null) return aPackage;
    if (aPackage.startsWith(currentTop)) {
      return currentTop;
    }
    int next;
    do {
      next = currentTop.lastIndexOf('.');
      if (next > -1) {
        currentTop = currentTop.substring(0, next);
        if (aPackage.startsWith(currentTop)) {
          return currentTop;
        }
      }
    } while (next > -1);

    return currentTop;
  }

  /**
   * Return the name via <code>@Named</code> or a Qualifier annotation.
   */
  public static String getNamed(Element p) {
    Named named = p.getAnnotation(Named.class);
    if (named != null) {
      return named.value();
    }
    for (AnnotationMirror annotationMirror : p.getAnnotationMirrors()) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      Qualifier qualifier = annotationType.asElement().getAnnotation(Qualifier.class);
      if (qualifier != null) {
        return Util.shortName(annotationType.toString());
      }
    }
    return null;
  }

  public static String addForInterface(String interfaceType) {
    if (interfaceType.contains("<")) {
      return null;
    }
    return shortName(interfaceType);
  }

  public static String trimMethod(String method) {
    return shortMethod(method)
      .replace('.','_')
      .replace("$di","");
  }
}
