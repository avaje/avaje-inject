package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.avaje.inject.prism.NamedPrism;
import io.avaje.inject.prism.QualifierPrism;

final class Util {

  static final String ASPECT_PROVIDER_PREFIX = "io.avaje.inject.aop.AspectProvider<";
  static final String PROVIDER_PREFIX = "jakarta.inject.Provider<";
  private static final String OPTIONAL_PREFIX = "java.util.Optional<";
  private static final String NULLABLE = "Nullable";
  private static final int PROVIDER_LENGTH = PROVIDER_PREFIX.length();
  private static final int ASPECT_PROVIDER_LENGTH = ASPECT_PROVIDER_PREFIX.length();

  static boolean isVoid(String type) {
    return "void".equalsIgnoreCase(type);
  }

  static boolean validImportType(String type) {
    return type.indexOf('.') > 0;
  }

  static String classOfMethod(String method) {
    return packageOf(method);
  }

  static String shortMethod(String method) {
    method = trimGenerics(method);
    int p = method.lastIndexOf('.');
    if (p > -1) {
      p = method.lastIndexOf('.', p - 1);
      if (p > -1) {
        return method.substring(p + 1);
      }
    }
    return method;
  }

  static String trimGenerics(String type) {
    int i = type.indexOf('<');
    if (i == -1) {
      return type;
    }
    return type.substring(0, i);
  }

  static String nestedPackageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    if (pos < 0) {
      return "";
    }
    pos = cls.lastIndexOf('.', pos - 1);
    return (pos == -1) ? "" : cls.substring(0, pos);
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

  static String initLower(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean upper = true;
    for (char ch : name.toCharArray()) {
      if (upper && Character.isUpperCase(ch)) {
        sb.append(Character.toLowerCase(ch));
      } else {
        upper = false;
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  static String nestedShortName(String fullType) {
    int pos = fullType.lastIndexOf('.');
    if (pos < 0) {
      return fullType;
    } else {
      pos = fullType.lastIndexOf('.', pos - 1);
      return pos < 0 ? fullType : fullType.substring(pos + 1);
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

  static String trimmedName(GenericType type) {
    return shortName(type.topType()).toLowerCase();
  }

  static boolean isOptional(String rawType) {
    return rawType.startsWith(OPTIONAL_PREFIX);
  }

  static String extractOptionalType(String rawType) {
    return rawType.substring(19, rawType.length() - 1);
  }

  static String extractList(String rawType) {
    String listType = rawType.substring(15, rawType.length() - 1);
    if (listType.startsWith("? extends")) {
      return listType.substring(10);
    }
    return listType;
  }

  static String extractSet(String rawType) {
    String setType = rawType.substring(14, rawType.length() - 1);
    if (setType.startsWith("? extends")) {
      return setType.substring(10);
    }
    return setType;
  }

  static String extractMap(String rawType) {
    String valType = rawType.substring(31, rawType.length() - 1);
    if (valType.startsWith("? extends")) {
      return valType.substring(10);
    }
    return valType;
  }

  static UtilType determineType(TypeMirror rawType) {
    return UtilType.of(rawType.toString());
  }

  static boolean isAspectProvider(String rawType) {
    return rawType.startsWith(ASPECT_PROVIDER_PREFIX);
  }

  static boolean isProvider(String rawType) {
    return rawType.startsWith(PROVIDER_PREFIX);
  }

  private static String extractProviderType(String rawType) {
    return rawType.substring(PROVIDER_LENGTH, rawType.length() - 1);
  }

  static String extractAspectType(String rawType) {
    return rawType.substring(ASPECT_PROVIDER_LENGTH, rawType.length() - 1);
  }

  static String wrapAspect(String aspect) {
    return Constants.ASPECT_PROVIDER + "<" + aspect + ">";
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
    NamedPrism named = NamedPrism.getInstanceOn(p);
    if (named != null) {
      return named.value().toLowerCase();
    }
    for (AnnotationMirror annotationMirror : p.getAnnotationMirrors()) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      QualifierPrism qualifier = QualifierPrism.getInstanceOn(annotationType.asElement());
      if (qualifier != null) {
        return Util.shortName(annotationType.toString()).toLowerCase();
      }
    }
    return null;
  }

  /**
   * Return true if the element has a Nullable annotation.
   */
  public static boolean isNullable(Element p) {
    for (AnnotationMirror mirror : p.getAnnotationMirrors()) {
      if (NULLABLE.equals(shortName(mirror.getAnnotationType().toString()))) {
        return true;
      }
    }
    return false;
  }

  public static String addForInterface(String interfaceType) {
    if (interfaceType.contains("<")) {
      return null;
    }
    return shortName(interfaceType);
  }

  public static String trimMethod(String method) {
    return shortMethod(method)
      .replace('.', '_')
      .replace(Constants.DI, "");
  }
}
