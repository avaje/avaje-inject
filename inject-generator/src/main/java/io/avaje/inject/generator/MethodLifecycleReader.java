package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/** Looks for lifecycle annotations on methods. */
final class MethodLifecycleReader {

  private final String initMethod;
  private final String destroyMethod;
  private Element postConstructMethod;
  private Element preDestroyMethod;
  private MethodReader initMethodReader;

  MethodLifecycleReader(TypeElement type, String initMethod, String destroyMethod, ImportTypeMap imports) {
    this.initMethod = initMethod;
    this.destroyMethod = destroyMethod;
    for (var element : ElementFilter.methodsIn(type.getEnclosedElements())) {
      if (element.getSimpleName().toString().equals(initMethod)
          || AnnotationUtil.hasAnnotationWithName(element, "PostConstruct")) {
        this.postConstructMethod = element;
        this.initMethodReader = new MethodReader(element, type, imports);
      }
      if (element.getSimpleName().toString().equals(destroyMethod)
          || AnnotationUtil.hasAnnotationWithName(element, "PreDestroy")) {
        this.preDestroyMethod = element;
      }
    }
  }

  String initMethod() {
    return deriveFromBoth(initMethod, postConstructMethod);
  }

  MethodReader initMethodReader() {
    return initMethodReader;
  }

  String destroyMethod() {
    return deriveFromBoth(destroyMethod, preDestroyMethod);
  }

  private String deriveFromBoth(String base, Element method) {
    return isEmpty(base) ? fromMethod(method) : base;
  }

  private String fromMethod(Element method) {
    return method == null ? null : method.getSimpleName().toString();
  }

  private boolean isEmpty(String val) {
    return val == null || val.isEmpty();
  }
}
