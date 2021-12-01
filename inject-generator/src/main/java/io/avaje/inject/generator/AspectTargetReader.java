package io.avaje.inject.generator;

import javax.lang.model.element.*;
import java.util.Set;
import java.util.stream.Collectors;

class AspectTargetReader {

  private final ProcessingContext context;
  private final TypeElement baseType;
  private final String target;
  private final TargetMethod method;
  private final String targetShortType;
  private final String targetName;

  AspectTargetReader(ProcessingContext context, TypeElement baseType, String target) {
    this.context = context;
    this.baseType = baseType;
    this.target = target;
    this.targetShortType = Util.shortName(target);
    this.targetName = Util.initLower(targetShortType);
    this.method = read(baseType);
  }

  TargetMethod read(TypeElement type) {
    for (Element element : type.getEnclosedElements()) {
      if (element.getKind() == ElementKind.METHOD) {
        return readMethod(element);
      }
    }
    return null;
  }

  private TargetMethod readMethod(Element element) {
    ExecutableElement methodElement = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(context, methodElement, baseType).read();
    Set<String> annotationTypes = readAnnotations(methodElement);
    return new TargetMethod(annotationTypes, methodReader);
  }

  private Set<String> readAnnotations(ExecutableElement methodElement) {
    return methodElement.getAnnotationMirrors().stream()
      .map(AnnotationMirror::getAnnotationType)
      .map(Object::toString)
      .collect(Collectors.toSet());
  }

  void writeBefore(Append writer) {
    if (method != null) {
      method.writeBefore(writer, targetName);
    }
  }

  static class TargetMethod {

    //private final Set<String> annotationTypes;
    final MethodReader methodReader;
    final boolean beforeAspect;
    final boolean aroundAspect;
    private final String methodName;

    TargetMethod(Set<String> annotationTypes, MethodReader methodReader) {
      //this.annotationTypes = annotationTypes;
      this.methodReader = methodReader;
      this.methodName = methodReader.getName();
      this.beforeAspect = annotationTypes.contains("io.avaje.inject.Aspect.Before");
      this.aroundAspect = annotationTypes.contains("io.avaje.inject.Aspect.Around");
    }

    void writeBefore(Append writer, String targetName) {
      if (beforeAspect) {
        methodReader.writeAspectBefore(writer, targetName);

      } else if (aroundAspect) {
        methodReader.writeAspectAround(writer, targetName);

      }
    }
  }
}
