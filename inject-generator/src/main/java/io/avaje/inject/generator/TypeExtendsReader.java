package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the inheritance types for a given bean type.
 */
class TypeExtendsReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private final GenericType baseGenericType;
  private final TypeElement baseType;
  private final ProcessingContext context;
  private final List<String> extendsTypes = new ArrayList<>();
  private final TypeExtendsInjection extendsInjection;
  private final List<String> interfaceTypes = new ArrayList<>();
  private final String beanSimpleName;
  private final String baseTypeRaw;
  private boolean closeable;
  /**
   * The implied qualifier name based on naming convention.
   */
  private String qualifierName;

  TypeExtendsReader(GenericType baseGenericType, TypeElement baseType, ProcessingContext context, boolean factory) {
    this.baseGenericType = baseGenericType;
    this.baseType = baseType;
    this.context = context;
    this.extendsInjection = new TypeExtendsInjection(baseType, context, factory);
    this.beanSimpleName = baseType.getSimpleName().toString();
    this.baseTypeRaw = Util.unwrapProvider(baseGenericType.toString());
  }

  GenericType getBaseType() {
    return baseGenericType;
  }

  List<String> getExtendsTypes() {
    return extendsTypes;
  }

  String getQualifierName() {
    return qualifierName;
  }

  BeanAspects hasAspects() {
    return extendsInjection.hasAspects();
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

  List<String> getProvides() {
    List<String> all = new ArrayList<>(extendsTypes.size() + interfaceTypes.size());
    all.addAll(extendsTypes);
    all.addAll(interfaceTypes);
    all.remove(baseTypeRaw);
    return all;
  }

  boolean isCloseable() {
    return closeable;
  }

  void process(boolean forBean) {
    extendsTypes.add(baseTypeRaw);
    if (forBean) {
      extendsInjection.read(baseType);
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
      if (isPublic(element)) {
        extendsTypes.add(type);
        extendsInjection.read(element);
      }
      addSuperType(superOf(element));
    }
  }

  private TypeElement superOf(TypeElement element) {
    return (TypeElement) context.asElement(element.getSuperclass());
  }

  private void readInterfaces(TypeElement type) {
    for (TypeMirror anInterface : type.getInterfaces()) {
      Element element = context.asElement(anInterface);
      if (isPublic(element)) {
        String rawType = Util.unwrapProvider(anInterface.toString());
        if (rawType.indexOf('.') == -1) {
          context.logWarn("skip when no package on interface " + rawType);
        } else if (Constants.AUTO_CLOSEABLE.equals(rawType) || Constants.IO_CLOSEABLE.equals(rawType)) {
          closeable = true;
        } else {
          String mainType = GenericType.removeParameter(rawType);
          if (qualifierName == null) {
            final String iShortName = Util.shortName(mainType);
            if (beanSimpleName.endsWith(iShortName)) {
              // derived qualifier name based on prefix to interface short name
              qualifierName = beanSimpleName.substring(0, beanSimpleName.length() - iShortName.length()).toLowerCase();
            }
          }
          interfaceTypes.add(rawType);
          readExtendedInterfaces(mainType);
        }
      }
    }
  }

  private boolean isPublic(Element element) {
    return element != null && element.getModifiers().contains(Modifier.PUBLIC);
  }

  private void readExtendedInterfaces(String type) {
    GenericType genericType = GenericType.parse(type);
    final TypeElement element = context.element(genericType.topType());
    if (element != null) {
      readInterfaces(element);
    }
  }
}
