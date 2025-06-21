package io.avaje.inject.generator;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

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
    return pos == -1 ? "" : method.substring(0, pos);
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
    return pos == -1 ? "" : cls.substring(0, pos);
  }

  static String unwrapProvider(String maybeProvider) {
    if (isProvider(maybeProvider)) {
      return extractProviderType(maybeProvider);
    } else {
      return maybeProvider;
    }
  }

  static UType unwrapProvider(TypeMirror maybeProvider) {
    return UType.parse(stripProvider(maybeProvider));
  }

  static UType unwrapProvider(UType maybeProvider) {
    if (isProvider(maybeProvider.mainType())) {
      return maybeProvider.param0();
    } else {
      return maybeProvider;
    }
  }

  static TypeMirror stripProvider(TypeMirror maybeProvider) {
    if (isProvider(maybeProvider.toString())) {
      return ((DeclaredType) maybeProvider).getTypeArguments().get(0);
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
    }

    String[] parts = fullType.split("\\.");
    StringBuilder result = new StringBuilder();
    boolean foundClass = false;

    for (String part : parts) {
      char firstChar = part.charAt(0);
      if (!foundClass && Character.isUpperCase(firstChar)) {
        foundClass = true;
      }
      if (foundClass) {
        if (result.length() > 0) {
          result.append(".");
        }
        result.append(part);
      }
    }

    return result.length() > 0 ? result.toString() : fullType.substring(p + 1);
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
    return UtilType.of(beanMap, rawType);
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
  static String named(Element p) {
    final NamedPrism named = NamedPrism.getInstanceOn(p);
    if (named != null) {
      String raw = named.value();
      if (raw.isEmpty()) {
        // default to the method name
        raw = p.getSimpleName().toString();
      }
      return raw.replace("\"", "\\\"");
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

  /**
   * Return true if the element has a Nullable annotation.
   */
  static boolean isNullable(Element element) {
    if (ProcessorUtils.hasAnnotationWithName(element, NULLABLE)) {
      return true;
    }
    for (final AnnotationMirror mirror : UType.parse(element.asType()).annotations()) {
      if (NULLABLE.equals(shortName(mirror.getAnnotationType().toString()))) {
        return true;
      }
    }
    return false;
  }

  static Optional<DeclaredType> nullableAnnotation(Element element) {
    for (final AnnotationMirror mirror : element.getAnnotationMirrors()) {
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

  static String valhalla() {
    try {
      if (Modifier.valueOf("VALUE") != null && APContext.previewEnabled()) return "value ";
    } catch (IllegalArgumentException e) {
      // no valhalla
    }
    return "";
  }

  static void validateBeanTypes(Element origin, List<TypeMirror> beanType) {
    TypeMirror targetType =
      origin instanceof TypeElement
        ? origin.asType()
        : ((ExecutableElement) origin).getReturnType();
    beanType.forEach(type -> {
      if (!APContext.types().isAssignable(targetType, type)) {
        APContext.logError(origin, "%s does not extend type %s", targetType, beanType);
      }
    });
  }

  static TypeElement lazyProxy(Element element) {
    TypeElement type =
      element instanceof TypeElement
        ? (TypeElement) element
        : APContext.asTypeElement(((ExecutableElement) element).getReturnType());

    if (type.getModifiers().contains(Modifier.FINAL)
        || !type.getKind().isInterface() && !Util.hasNoArgConstructor(type)) {

      return BeanTypesPrism.getOptionalOn(element)
          .map(BeanTypesPrism::value)
          .filter(v -> v.size() == 1)
          .map(v -> APContext.asTypeElement(v.get(0)))
          // generics and beantypes don't mix
          .filter(t -> t.getTypeParameters().isEmpty() || t.equals(element))
          .filter(v -> (v.getKind().isInterface() || hasNoArgConstructor(v)))
          .orElse(null);
    }

    return type;
  }

  static boolean hasNoArgConstructor(TypeElement beanType) {
    return ElementFilter.constructorsIn(beanType.getEnclosedElements()).stream()
      .anyMatch(e -> e.getParameters().isEmpty() && !e.getModifiers().contains(Modifier.PRIVATE));
  }

  public static String shortNameLazyProxy(TypeElement lazyProxyType) {
    return shortName(lazyProxyType.getQualifiedName().toString())
      .replace(".", "_");  }
}
