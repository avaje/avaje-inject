package io.avaje.inject.generator;

import io.avaje.inject.Aspect;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import java.util.Map;

/**
 * Read the annotations on the type.
 */
class AspectAnnotationReader {

  private static final String ASPECT = "io.avaje.inject.Aspect";
  private final TypeElement baseType;
  private final Element element;
  private final ProcessingContext context;

  AspectAnnotationReader(ProcessingContext context, TypeElement baseType, Element element) {
    this.context = context;
    this.baseType = baseType;
    this.element = element;
  }

  AspectPair read() {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      Element anElement = annotationMirror.getAnnotationType().asElement();
      Aspect aspect = anElement.getAnnotation(Aspect.class);
      if (aspect != null) {
        String target = readTarget(anElement);
        if (target != null) {
          context.logDebug(baseType + " " + element + " has aspect:" + anElement + " target:" + target);
          return new AspectPair(anElement, target);
        }
      }
    }
    return null;
  }

  private String readTarget(Element anElement) {
    for (AnnotationMirror annotationMirror : anElement.getAnnotationMirrors()) {
      if (ASPECT.equals(annotationMirror.getAnnotationType().toString())) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
          if (entry.getKey().toString().equals("target()")) {
            return entry.getValue().getValue().toString();
          }
        }
      }
    }
    context.logError(baseType + " aspect target() not found on " + element);
    return null;
  }
}
