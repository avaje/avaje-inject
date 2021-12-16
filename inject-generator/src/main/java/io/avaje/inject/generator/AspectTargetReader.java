package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

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
    return new TargetMethod(methodReader);
  }

//  private Set<String> readAnnotations(ExecutableElement methodElement) {
//    return methodElement.getAnnotationMirrors().stream()
//      .map(AnnotationMirror::getAnnotationType)
//      .map(Object::toString)
//      .collect(Collectors.toSet());
//  }

  void writeBefore(Append writer, AspectMethod aspectMethod) {
    if (method != null) {
      method.writeBefore(writer, aspectMethod);
    }
  }

  void writeAfter(Append writer, AspectMethod aspectMethod) {
    if (method != null) {
      method.writeAfter(writer, aspectMethod);
    }
  }

  static class TargetMethod {

    final MethodReader methodReader;

    TargetMethod(MethodReader methodReader) {
      this.methodReader = methodReader;
    }

    void writeBefore(Append writer, AspectMethod aspectMethod) {
      boolean aVoid = aspectMethod.isVoid();
      String type = aVoid ? "Run" : "Call<>";
      writer.append("    var call = new Invocation.%s(() ->", type);
    }

    void writeAfter(Append writer, AspectMethod aspectMethod) {
      aspectMethod.writeArgs(writer);
    }
  }
}
