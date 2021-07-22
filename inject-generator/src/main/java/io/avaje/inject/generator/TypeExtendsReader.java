package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the inheritance types for a given bean type.
 */
class TypeExtendsReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private final TypeElement baseType;
  private final ProcessingContext context;
  private final List<String> extendsTypes = new ArrayList<>();
  private final TypeExtendsInjection extendsInjection;
  private final List<String> interfaceTypes = new ArrayList<>();
  private final String beanSimpleName;
  private boolean closeable;
  /**
   * The implied qualifier name based on naming convention.
   */
  private String qualifierName;
  private String baseTypeRaw;

  TypeExtendsReader(TypeElement baseType, ProcessingContext context, boolean factory) {
    this.baseType = baseType;
    this.context = context;
    this.extendsInjection = new TypeExtendsInjection(baseType, context, factory);
    this.beanSimpleName = baseType.getSimpleName().toString();
  }

  String getBaseType() {
    return baseTypeRaw;
  }

  List<String> getExtendsTypes() {
    return extendsTypes;
  }

  String getQualifierName() {
    return qualifierName;
  }

  void process(boolean forBean) {
    String base = Util.unwrapProvider(baseType.getQualifiedName().toString());
    if (!GenericType.isGeneric(base)) {
      baseTypeRaw = base;
      extendsTypes.add(base);
      if (forBean) {
        extendsInjection.read(baseType);
      }
    }
    readInterfaces(baseType);
    TypeElement superElement = superOf(baseType);
    if (superElement != null) {
      if (qualifierName == null) {
        String baseName = baseType.getSimpleName().toString();
        String superName = superElement.getSimpleName().toString();
        if (baseName.endsWith(superName)) {
          qualifierName = baseName.substring(0, baseName.length() - superName.length()).toLowerCase();
        }
      }
      addSuperType(superElement);
    }
  }

  private void addSuperType(TypeElement element) {
    readInterfaces(element);
    String fullName = element.getQualifiedName().toString();
    if (!fullName.equals(JAVA_LANG_OBJECT)) {
      String type = Util.unwrapProvider(fullName);
      if (!GenericType.isGeneric(type)) {
        extendsTypes.add(type);
        extendsInjection.read(element);
        addSuperType(superOf(element));
      }
    }
  }

  private TypeElement superOf(TypeElement element) {
    return (TypeElement) context.asElement(element.getSuperclass());
  }

  List<FieldReader> getInjectFields() {
    return extendsInjection.getInjectFields();
  }

  List<MethodReader> getInjectMethods() {
    return extendsInjection.getInjectMethods();
  }

  List<MethodReader> getFactoryMethods() {
    return extendsInjection.getFactoryMethods();
  }

  Element getPostConstructMethod() {
    return extendsInjection.getPostConstructMethod();
  }

  Element getPreDestroyMethod() {
    return extendsInjection.getPreDestroyMethod();
  }

  MethodReader getConstructor() {
    return extendsInjection.getConstructor();
  }

  List<String> getInterfaceTypes() {
    return interfaceTypes;
  }

  boolean isCloseable() {
    return closeable;
  }

  private void readInterfaces(TypeElement type) {
    for (TypeMirror anInterface : type.getInterfaces()) {
      String rawType = Util.unwrapProvider(anInterface.toString());
      if (rawType.indexOf('.') == -1) {
        context.logWarn("skip when no package on interface " + rawType);
      } else if (Constants.AUTO_CLOSEABLE.equals(rawType) || Constants.IO_CLOSEABLE.equals(rawType)) {
        closeable = true;
      } else {
        if (qualifierName == null) {
          final String iShortName = Util.shortName(rawType);
          if (beanSimpleName.endsWith(iShortName)) {
            // derived qualifier name based on prefix to interface short name
            qualifierName = beanSimpleName.substring(0, beanSimpleName.length() - iShortName.length()).toLowerCase();
          }
        }
        interfaceTypes.add(rawType);
        readExtendedInterfaces(rawType);
      }
    }
  }

  private void readExtendedInterfaces(String type) {
    final TypeElement element = context.element(type);
    if (element != null) {
      readInterfaces(element);
    }
  }
}
