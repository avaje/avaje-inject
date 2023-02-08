package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the annotations on the type.
 */
final class TypeAnnotationReader {

  private final TypeElement beanType;
  private final ProcessingContext context;
  private final List<String> annotationTypes = new ArrayList<>();
  private String qualifierName;

  TypeAnnotationReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.context = context;
  }

  List<String> annotationTypes() {
    return annotationTypes;
  }

  boolean hasQualifierName() {
    return qualifierName != null;
  }

  String qualifierName() {
    return qualifierName;
  }

  void process() {
    for (AnnotationMirror annotationMirror : beanType.getAnnotationMirrors()) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      String annType = annotationType.toString();
      if (context.hasAnnotation(annotationType.asElement(), Constants.QUALIFIER)) {
        qualifierName = Util.shortName(annType).toLowerCase();
      } else if (annType.indexOf('.') == -1) {
        context.logWarn("skip when no package on annotation " + annType);
      } else if (IncludeAnnotations.include(annType)) {
        annotationTypes.add(annType);
      }
    }
  }
}
