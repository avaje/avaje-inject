package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ScopeUtil {

  private static final String INJECT_MODULE = "io.avaje.inject.InjectModule";

  /**
   * Read the list of required class names.
   */
  static List<String> readRequires(Element element) {
    List<String> requiresList = new ArrayList<>();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (INJECT_MODULE.equals(annotationMirror.getAnnotationType().toString())) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
          if (entry.getKey().toString().startsWith("requires")) {
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

}
