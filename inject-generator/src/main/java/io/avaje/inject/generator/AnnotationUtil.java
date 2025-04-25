package io.avaje.inject.generator;

import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

final class AnnotationUtil {

  static boolean hasAnnotationWithName(Element element, String matchShortName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (matchShortName.equals(shortName(mirror.getAnnotationType().asElement()))) {
        return true;
      }
    }

    if (element instanceof ExecutableElement) {
      return annotatedSuperMethod((ExecutableElement) element, matchShortName);
    }

    return false;
  }

  /** Return the short name of the element. */
  private static String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  private static boolean annotatedSuperMethod(ExecutableElement element, Object matchShortName) {
    var methodName = element.getSimpleName();
    final Types types = APContext.types();
    return types.directSupertypes(element.getEnclosingElement().asType()).stream()
        .filter(type -> !type.toString().contains("java.lang.Object"))
        .map(
            superType -> {
              final var superClass = (TypeElement) types.asElement(superType);
              for (final var method : ElementFilter.methodsIn(superClass.getEnclosedElements())) {
                if (method.getSimpleName().contentEquals(methodName)
                    && method.getParameters().size() == element.getParameters().size()) {
                  return method;
                }
              }
              return null;
            })
        .filter(Objects::nonNull)
        .flatMap(m -> APContext.elements().getAllAnnotationMirrors(m).stream())
        .anyMatch(m -> matchShortName.equals(shortName(m.getAnnotationType().asElement())));
  }
}
