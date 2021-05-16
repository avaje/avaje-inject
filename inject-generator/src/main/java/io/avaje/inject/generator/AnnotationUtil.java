package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

class AnnotationUtil {

  static boolean hasAnnotationWithName(Element element, String matchShortName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (matchShortName.equals(shortName(mirror.getAnnotationType().asElement()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return the short name of the element.
   */
  private static String shortName(Element element) {
    return element.getSimpleName().toString();
  }
}
