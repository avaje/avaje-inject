package io.avaje.inject.generator;

import jakarta.inject.Qualifier;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the annotations on the type.
 */
class TypeAnnotationReader {

  private final TypeElement beanType;
  private final ProcessingContext context;
  private final List<String> annotationTypes = new ArrayList<>();
  private String qualifierName;

  TypeAnnotationReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.context = context;
  }

  List<String> getAnnotationTypes() {
    return annotationTypes;
  }

  boolean hasQualifierName() {
    return qualifierName != null;
  }

  String getQualifierName() {
    return qualifierName;
  }

  void process() {
    for (AnnotationMirror annotationMirror : beanType.getAnnotationMirrors()) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      Qualifier qualifier = annotationType.asElement().getAnnotation(Qualifier.class);
      String annType = annotationType.toString();
      if (qualifier != null) {
        qualifierName = Util.shortName(annType).toLowerCase();
      } else if (annType.indexOf('.') == -1) {
        context.logWarn("skip when no package on annotation " + annType);
      } else {
        if (IncludeAnnotations.include(annType)) {
          annotationTypes.add(annType);
        }
      }
    }
  }
}
