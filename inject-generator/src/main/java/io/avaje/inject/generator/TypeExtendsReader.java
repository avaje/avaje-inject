package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the inheritance types for a given bean type.
 */
final class TypeExtendsReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private final GenericType baseGenericType;
  private final TypeElement baseType;
  private final ProcessingContext context;
  private final TypeExtendsInjection extendsInjection;
  private final List<String> extendsTypes = new ArrayList<>();
  private final List<String> interfaceTypes = new ArrayList<>();
  private final List<String> providesTypes = new ArrayList<>();
  private final String beanSimpleName;
  private final String baseTypeRaw;
  private final boolean baseTypeIsInterface;
  private boolean closeable;
  /**
   * The implied qualifier name based on naming convention.
   */
  private String qualifierName;

  TypeExtendsReader(GenericType baseGenericType, TypeElement baseType, ProcessingContext context, boolean factory, ImportTypeMap importTypes) {
    this.baseGenericType = baseGenericType;
    this.baseType = baseType;
    this.context = context;
    this.extendsInjection = new TypeExtendsInjection(baseType, context, factory, importTypes);
    this.beanSimpleName = baseType.getSimpleName().toString();
    this.baseTypeRaw = Util.unwrapProvider(baseGenericType.toString());
    this.baseTypeIsInterface = baseType.getKind() == ElementKind.INTERFACE;
  }

  GenericType baseType() {
    return baseGenericType;
  }

  String qualifierName() {
    return qualifierName;
  }

  BeanAspects hasAspects() {
    return extendsInjection.hasAspects();
  }

  List<FieldReader> injectFields() {
    return extendsInjection.injectFields();
  }

  List<MethodReader> injectMethods() {
    return extendsInjection.injectMethods();
  }

  List<MethodReader> factoryMethods() {
    return extendsInjection.factoryMethods();
  }

  Element postConstructMethod() {
    return extendsInjection.postConstructMethod();
  }

  Element preDestroyMethod() {
    return extendsInjection.preDestroyMethod();
  }

  MethodReader constructor() {
    return extendsInjection.constructor();
  }

  String autoProvides() {
    if (baseTypeIsInterface) {
      return baseTypeRaw;
    }
    if (!interfaceTypes.isEmpty()) {
      return interfaceTypes.get(0);
    }
    return null;
  }

  List<String> provides() {
    return providesTypes;
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

    providesTypes.addAll(extendsTypes);
    providesTypes.addAll(interfaceTypes);
    providesTypes.remove(baseTypeRaw);
    // we can't provide a type that is getting injected
    extendsInjection.removeFromProvides(providesTypes);
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
      if (isPublic(context.asElement(anInterface))) {
        readInterfacesOf(anInterface);
      }
    }
  }

  private void readInterfacesOf(TypeMirror anInterface) {
    String rawType = Util.unwrapProvider(anInterface.toString());
    if (JAVA_LANG_OBJECT.equals(rawType)) {
      return;
    }
    if (rawType.indexOf('.') == -1) {
      context.logWarn("skip when no package on interface " + rawType);
    } else if (Constants.AUTO_CLOSEABLE.equals(rawType) || Constants.IO_CLOSEABLE.equals(rawType)) {
      closeable = true;
    } else {
      if (qualifierName == null) {
        String mainType = GenericType.removeParameter(rawType);
        final String iShortName = Util.shortName(mainType);
        if (beanSimpleName.endsWith(iShortName)) {
          // derived qualifier name based on prefix to interface short name
          qualifierName = beanSimpleName.substring(0, beanSimpleName.length() - iShortName.length()).toLowerCase();
        }
      }
      interfaceTypes.add(rawType);
      for (TypeMirror supertype : context.types().directSupertypes(anInterface)) {
        readInterfacesOf(supertype);
      }
    }
  }

  private boolean isPublic(Element element) {
    return element != null && element.getModifiers().contains(Modifier.PUBLIC);
  }
}
