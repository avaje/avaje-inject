package io.avaje.inject.generator;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * Read interface types for a given bean type.
 */
class TypeInterfaceReader {

  private final TypeElement beanType;
  private final ProcessingContext context;
  private final List<String> interfaceTypes = new ArrayList<>();
  private boolean beanLifeCycle;

  /**
   * Create for bean type.
   */
  TypeInterfaceReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.context = context;
  }

  List<String> getInterfaceTypes() {
    return interfaceTypes;
  }

  boolean isBeanLifeCycle() {
    return beanLifeCycle;
  }

  void process() {
    if (beanType == null) {
      return;
    }
    if (beanType.getKind() == ElementKind.INTERFACE) {
      interfaceTypes.add(beanType.getQualifiedName().toString());
    }
    for (TypeMirror anInterface : beanType.getInterfaces()) {
      String type = Util.unwrapProvider(anInterface.toString());
      if (Constants.isBeanLifecycle(type)) {
        beanLifeCycle = true;
      } else if (type.indexOf('.') == -1) {
        context.logWarn("skip when no package on interface " + type);
      } else {
        interfaceTypes.add(type);
      }
    }
  }
}
