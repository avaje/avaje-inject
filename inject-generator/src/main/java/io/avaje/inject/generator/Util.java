package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class Util {
  static final String ASPECT_PROVIDER_PREFIX = "io.avaje.inject.aop.AspectProvider<";
  static final String PROVIDER_PREFIX = "jakarta.inject.Provider";
  private static final String OPTIONAL_PREFIX = "java.util.Optional<";
  private static final String NULLABLE = "Nullable";
  private static final int PROVIDER_LENGTH = PROVIDER_PREFIX.length() + 1;
  private static final int ASPECT_PROVIDER_LENGTH = ASPECT_PROVIDER_PREFIX.length();

  static boolean notJavaLang(String type) {
    return !type.startsWith("java.lang.") || Character.isLowerCase(type.charAt(10));
  }

  static boolean isVoid(String type) {
    return "void".equalsIgnoreCase(type);
  }

  static boolean validImportType(String type, String packageName) {
    return type.indexOf('.') > -1
      && !type.startsWith("java.lang.")
      && importDifferentPackage(type, packageName)
      || importJavaLangSubpackage(type);
  }

  private static boolean importDifferentPackage(String type, String packageName) {
    return type.replace(packageName + '.', "").indexOf('.') > -1;
  }

  private static boolean importJavaLangSubpackage(String type) {
    return type.startsWith("java.lang.") && importDifferentPackage(type, "java.lang");
  }

  static String classOfMethod(String method) {
    final int pos = method.lastIndexOf('.');
    return (pos == -1) ? "" : method.substring(0, pos);
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
    final int i = type.indexOf('<');
    if (i == -1) {
      return type;
    }
    return type.substring(0, i);
  }

  public static String sanitizeImports(String type) {
    final int pos = type.indexOf("@");
    if (pos == -1) {
      return trimArrayBrackets(type);
    }
    final var start = pos == 0 ? type.substring(0, pos) : "";
    return start + trimArrayBrackets(type.substring(type.lastIndexOf(' ') + 1));
  }

  private static String trimArrayBrackets(String type) {
    return type.replaceAll("[^\\n\\r\\t $;\\w.]", "");
  }

  static String nestedPackageOf(String cls) {
    int pos = cls.lastIndexOf('.');
    if (pos < 0) {
      return "";
    }
    pos = cls.lastIndexOf('.', pos - 1);
    return (pos == -1) ? "" : cls.substring(0, pos);
  }

  static String unwrapProvider(String maybeProvider) {
    if (isProvider(maybeProvider)) {
      return extractProviderType(maybeProvider);
    } else {
      return maybeProvider;
    }
  }

  static UType unwrapProvider(TypeMirror maybeProvider) {
    if (isProvider(maybeProvider.toString())) {
      return UType.parse(maybeProvider).param0();
    } else {
      return UType.parse(maybeProvider);
    }
  }

  static UType unwrapProvider(UType maybeProvider) {
    if (isProvider(maybeProvider.mainType())) {
      return maybeProvider.param0();
    } else {
      return maybeProvider;
    }
  }

  static String initLower(String name) {
    final StringBuilder sb = new StringBuilder(name.length());
    boolean upper = true;
    for (final char ch : name.toCharArray()) {
      if (upper && Character.isUpperCase(ch)) {
        sb.append(Character.toLowerCase(ch));
      } else {
        upper = false;
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  static String shortName(String fullType) {
    final int p = fullType.lastIndexOf('.');
    if (p == -1) {
      return fullType;
    } else if (fullType.startsWith("java")) {
      return fullType.substring(p + 1);
    } else {
      var result = "";
      var foundClass = false;
      for (final String part : fullType.split("\\.")) {
        char firstChar = part.charAt(0);
        if (foundClass
          || Character.isUpperCase(firstChar)
          || (!Character.isAlphabetic(firstChar) && Character.isJavaIdentifierStart(firstChar))) {
          foundClass = true;
          result += (result.isEmpty() ? "" : ".") + part;
        }
      }
      // when in doubt, do the basic thing
      if (result.isBlank()) {
        return fullType.substring(p + 1);
      }
      return result;
    }
  }

  static String shortName(UType uType) {
    StringBuilder sb = new StringBuilder();
    shortName(uType, sb);
    return sb.toString();
  }

  static void shortName(UType uType, StringBuilder sb) {
    var type = trimWildcard(uType.mainType());
    if (type != null && type.startsWith("? extends ")) {
      type = type.substring(10);
    } else if ("?".equals(type)) {
      type = "Wildcard";
    } else if (!type.contains(".")) {
      return;
    }
    sb.append(Util.shortName(type));
    final var componentTypes = uType.componentTypes();
    if (componentTypes.size() != 1 || componentTypes.get(0).kind() != TypeKind.WILDCARD)
      for (UType param : componentTypes) {
        shortName(param, sb);
      }
  }

  static String trimmedName(UType type) {
    return shortName(type.mainType()).toLowerCase();
  }

  String trimExtends(UType uType) {
    String type = uType.mainType();
    if (type != null && type.startsWith("? extends ")) {
      return type.substring(10);
    } else if ("?".equals(type)) {
      return "Wildcard";
    }
    return type;
  }

  static boolean isOptional(String rawType) {
    return rawType.startsWith(OPTIONAL_PREFIX);
  }

  static String extractOptionalType(String rawType) {
    return rawType.substring(19, rawType.length() - 1);
  }

  static String extractList(String rawType) {
    final String listType = rawType.substring(15, rawType.length() - 1);
    if (listType.startsWith("? extends")) {
      return listType.substring(10);
    }
    return listType;
  }

  static String extractSet(String rawType) {
    final String setType = rawType.substring(14, rawType.length() - 1);
    if (setType.startsWith("? extends")) {
      return setType.substring(10);
    }
    return setType;
  }

  static String extractMap(String rawType) {
    final String valType = rawType.substring(31, rawType.length() - 1);
    if (valType.startsWith("? extends")) {
      return valType.substring(10);
    }
    return valType;
  }

  static UtilType determineType(TypeMirror rawType, boolean beanMap) {
    return UtilType.of(rawType.toString(), beanMap, rawType);
  }

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
  static String getNamed(Element p) {
    final NamedPrism named = NamedPrism.getInstanceOn(p);
    if (named != null) {
      return named.value().replace("\"", "\\\"");
    }
    for (final AnnotationMirror annotationMirror : p.getAnnotationMirrors()) {
      final DeclaredType annotationType = annotationMirror.getAnnotationType();
      final var hasQualifier = QualifierPrism.isPresent(annotationType.asElement());
      if (hasQualifier) {
        var shortName = Util.shortName(annotationType.toString());

        return AnnotationCopier.toSimpleAnnotationString(annotationMirror)
          .replaceFirst(annotationType.toString(), shortName)
          .replace("\"", "\\\"");
      }
    }
    return null;
  }

  /** Return true if the element has a Nullable annotation. */
  static boolean isNullable(Element p) {

    if (ProcessorUtils.hasAnnotationWithName(p, NULLABLE)) {
      return true;
    }

    for (final AnnotationMirror mirror : UType.parse(p.asType()).annotations()) {
      if (NULLABLE.equals(shortName(mirror.getAnnotationType().toString()))) {
        return true;
      }
    }
    return false;
  }

  static Optional<DeclaredType> getNullableAnnotation(Element p) {
    for (final AnnotationMirror mirror : p.getAnnotationMirrors()) {
      if (NULLABLE.equals(shortName(mirror.getAnnotationType().toString()))) {
        return Optional.of(mirror.getAnnotationType());
      }
    }
    return Optional.empty();
  }

  static String addForInterface(String interfaceType) {
    if (interfaceType.contains("<")) {
      return null;
    }
    return shortName(interfaceType);
  }

  static String trimMethod(String method) {
    return shortMethod(method).replace('.', '_').replace(Constants.DI, "");
  }

  private static final Pattern ANNOTATION_TYPE_PATTERN = Pattern.compile("@([\\w.]+)\\.");

  static String trimAnnotationString(String input) {
    return ANNOTATION_TYPE_PATTERN.matcher(input).replaceAll("@");
  }

  static String addQualifierSuffixTrim(String named, String type) {
    return addQualifierSuffix(named, type).replace(", ", ",");
  }

  static String addQualifierSuffix(String named, String type) {
    return type +
      Optional.ofNullable(named)
        .filter(not(String::isBlank))
        .map(s -> ":" + s)
        .orElse("");
  }

  static List<String> addQualifierSuffix(List<String> provides, String name) {
    return Stream.concat(
        provides.stream().map(s -> Util.addQualifierSuffix(name, s)),
        provides.stream())
      .distinct()
      .collect(toList());
  }

}
