package io.avaje.inject.generator;


import static io.avaje.inject.generator.ProcessingContext.logWarn;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

/**
 * Read the annotations on the type.
 */
final class TypeAnnotationReader {

  private final TypeElement beanType;
  private final List<String> annotationTypes = new ArrayList<>();
  private String qualifierName;

  TypeAnnotationReader(TypeElement beanType) {
    this.beanType = beanType;
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
      
      if (QualifierPrism.isPresent(annotationType.asElement())) {
        qualifierName = Util.shortName(annType).toLowerCase();
      } else if (annType.indexOf('.') == -1) {
        logWarn("skip when no package on annotation " + annType);
      } else if (IncludeAnnotations.include(annType)) {
        annotationTypes.add(annType);
      }
    }
  }
}
