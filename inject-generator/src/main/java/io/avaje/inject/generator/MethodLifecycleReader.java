package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Looks for lifecycle annotations on methods.
 */
class MethodLifecycleReader {

  private final String initMethod;
  private final String destroyMethod;
  private Element postConstructMethod;
  private Element preDestroyMethod;


  MethodLifecycleReader(TypeElement type, String initMethod, String destroyMethod) {
    this.initMethod = initMethod;
    this.destroyMethod = destroyMethod;
    for (Element element : type.getEnclosedElements()) {
      ElementKind kind = element.getKind();
      if (kind == ElementKind.METHOD) {
        readMethod(element);
      }
    }
  }

  private void readMethod(Element element) {
    if (AnnotationUtil.hasAnnotationWithName(element, "PostConstruct")) {
      postConstructMethod = element;
    }
    if (AnnotationUtil.hasAnnotationWithName(element, "PreDestroy")) {
      preDestroyMethod = element;
    }
  }

  String initMethod() {
    return deriveFromBoth(initMethod, postConstructMethod);
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
