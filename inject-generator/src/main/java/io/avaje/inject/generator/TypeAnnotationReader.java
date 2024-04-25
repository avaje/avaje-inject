package io.avaje.inject.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import static io.avaje.inject.generator.APContext.logWarn;

/**
 * Read the annotations on the type.
 */
final class TypeAnnotationReader {

  private final TypeElement beanType;
  private String qualifierName;

  TypeAnnotationReader(TypeElement beanType) {
    this.beanType = beanType;
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
        var shortName = Util.shortName(annotationType.toString());
        qualifierName = AnnotationCopier.toSimpleAnnotationString(annotationMirror)
          .replaceFirst(annotationType.toString(), shortName)
          .replace("\"", "\\\"");

      } else if (annType.indexOf('.') == -1) {
        logWarn("skip when no package on annotation " + annType);
      }
    }
  }
}
