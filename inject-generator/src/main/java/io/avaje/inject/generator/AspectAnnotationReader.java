package io.avaje.inject.generator;

import io.avaje.inject.aop.Aspect;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read the annotations on the type.
 */
class AspectAnnotationReader {

  private static final String ASPECT = Constants.ASPECT;
  private final TypeElement baseType;
  private final Element element;
  private final ProcessingContext context;

  AspectAnnotationReader(ProcessingContext context, TypeElement baseType, Element element) {
    this.context = context;
    this.baseType = baseType;
    this.element = element;
  }

  List<AspectPair> read() {
    List<AspectPair> aspects = new ArrayList<>();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      Element anElement = annotationMirror.getAnnotationType().asElement();
      Aspect aspect = anElement.getAnnotation(Aspect.class);
      if (aspect != null) {
        Meta meta = readTarget(anElement);
        if (meta != null) {
          context.logDebug(baseType + " " + element + " has aspect:" + anElement + " " + meta);
          aspects.add(new AspectPair(anElement, meta.target, meta.ordering));
        }
      }
    }
    return aspects;
  }

  private Meta readTarget(Element anElement) {
    for (AnnotationMirror annotationMirror : anElement.getAnnotationMirrors()) {
      if (ASPECT.equals(annotationMirror.getAnnotationType().toString())) {
        Meta meta = new Meta();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
          String key = entry.getKey().toString();
          if (key.equals("target()")) {
            meta.target(entry.getValue().getValue().toString());
          } else if (key.equals("ordering()")) {
            meta.ordering(Integer.parseInt(entry.getValue().getValue().toString()));
          }
        }
        return meta;
      }
    }
    context.logError(baseType + " aspect target() not found on " + element);
    return null;
  }

  private static class Meta {

    String target;

    int ordering = Constants.ORDERING_DEFAULT;

    void target(String target) {
      this.target = target;
    }

    void ordering(int ordering) {
      this.ordering = ordering;
    }

    @Override
    public String toString() {
      return "Meta(target=" + target + ", ordering=" + ordering + ')';
    }
  }
}
