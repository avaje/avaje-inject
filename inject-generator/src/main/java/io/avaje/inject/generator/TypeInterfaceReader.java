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
  private final String beanSimpleName;
  private boolean beanLifeCycle;
  private boolean closeable;
  private String qualifierName;

  /**
   * Create for bean type.
   */
  TypeInterfaceReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.context = context;
    this.beanSimpleName = beanType.getSimpleName().toString();
  }

  List<String> getInterfaceTypes() {
    return interfaceTypes;
  }

  boolean isBeanLifeCycle() {
    return beanLifeCycle;
  }

  boolean isCloseable() {
    return closeable;
  }

  String getQualifierName() {
    return qualifierName;
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
      } else if (Constants.IO_CLOSEABLE.equals(type)) {
        closeable = true;
      } else {
        final String iShortName = Util.shortName(type);
        if (beanSimpleName.endsWith(iShortName)) {
          // derived qualifier name based on prefix to interface short name
          qualifierName = beanSimpleName.substring(0, beanSimpleName.length() - iShortName.length()).toLowerCase();
        }
        interfaceTypes.add(type);
      }
    }
  }
}
