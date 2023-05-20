package io.avaje.inject.generator;

import static io.avaje.inject.generator.ProcessingContext.asElement;
import static io.avaje.inject.generator.ProcessingContext.element;
import static io.avaje.inject.generator.ProcessingContext.logWarn;
import static io.avaje.inject.generator.ProcessingContext.types;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Read the inheritance types for a given bean type.
 */
final class TypeExtendsReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private static final String JAVA_LANG_RECORD = "java.lang.Record";
  private final GenericType baseGenericType;
  private final TypeElement baseType;
  private final TypeExtendsInjection extendsInjection;
  private final List<String> extendsTypes = new ArrayList<>();
  private final List<String> interfaceTypes = new ArrayList<>();
  private final List<String> providesTypes = new ArrayList<>();
  private final String beanSimpleName;
  private final String baseTypeRaw;
  private final boolean baseTypeIsInterface;
  private final boolean publicAccess;
  private final boolean autoProvide;
  private boolean closeable;
  /**
   * The implied qualifier name based on naming convention.
   */
  private String qualifierName;
  private String providesAspect = "";

  TypeExtendsReader(GenericType baseGenericType, TypeElement baseType, boolean factory, ImportTypeMap importTypes) {
    this.baseGenericType = baseGenericType;
    this.baseType = baseType;
    this.extendsInjection = new TypeExtendsInjection(baseType, factory, importTypes);
    this.beanSimpleName = baseType.getSimpleName().toString();
    this.baseTypeRaw = Util.unwrapProvider(baseGenericType.toString());
    this.baseTypeIsInterface = baseType.getKind() == ElementKind.INTERFACE;
    this.publicAccess = baseType.getModifiers().contains(Modifier.PUBLIC);
    this.autoProvide = autoProvide();
  }

  private boolean autoProvide() {
    return publicAccess
        && !FactoryPrism.isPresent(baseType)
        && !ProxyPrism.isPresent(baseType)
        && !GeneratedPrism.isPresent(baseType)
        && !isController();
  }

  private boolean isController() {
    try {
      return baseType.getAnnotation((Class<Annotation>) Class.forName(Constants.CONTROLLER)) != null;
    } catch (final ClassNotFoundException e) {
      return false;
    }
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

  String providesAspect() {
    return providesAspect;
  }

  String autoProvides() {
    if (!autoProvide || !providesAspect.isEmpty()) {
      return null;
    }
    if (baseTypeIsInterface || interfaceTypes.isEmpty()) {
      return baseTypeRaw;
    }
    return interfaceTypes.get(0);
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
    final var superMirror = baseType.getSuperclass();
    final TypeElement superElement = asElement(superMirror);

    if (superElement != null) {
      if (qualifierName == null) {
        final String baseName = baseType.getSimpleName().toString();
        final String superName = superElement.getSimpleName().toString();
        if (baseName.endsWith(superName)) {
          qualifierName = baseName.substring(0, baseName.length() - superName.length()).toLowerCase();
        }
      }
      addSuperType(superElement,superMirror);
    }

    providesTypes.addAll(extendsTypes);
    providesTypes.addAll(interfaceTypes);
    providesTypes.remove(baseTypeRaw);
    // we can't provide a type that is getting injected
    extendsInjection.removeFromProvides(providesTypes);
    providesAspect = initProvidesAspect();
  }

  private String initProvidesAspect() {
    for (final String providesType : providesTypes) {
      if (Util.isAspectProvider(providesType)) {
        return Util.extractAspectType(providesType);
      }
    }
    return "";
  }

  private void addSuperType(TypeElement element, TypeMirror mirror) {
    readInterfaces(element);
    final String fullName = mirror.toString();
    if (!JAVA_LANG_OBJECT.equals(fullName) && !JAVA_LANG_RECORD.equals(fullName)) {
      final String type = Util.unwrapProvider(fullName);
      if (isPublic(element)) {
        extendsTypes.add(type);
        extendsInjection.read(element);
      }

      final var superMirror = element.getSuperclass();
      final TypeElement superElement = asElement(superMirror);
      addSuperType(superElement, superMirror);
    }
  }

  private void readInterfaces(TypeElement type) {
    for (final TypeMirror anInterface : type.getInterfaces()) {
      if (isPublic(asElement(anInterface))) {
        readInterfacesOf(anInterface);
      }
    }
  }

  private void readInterfacesOf(TypeMirror anInterface) {
    final String rawType = Util.unwrapProvider(anInterface.toString());
    if (JAVA_LANG_OBJECT.equals(rawType)) {
    } else if (rawType.indexOf('.') == -1) {
      logWarn("skip when no package on interface " + rawType);
    } else if (Constants.AUTO_CLOSEABLE.equals(rawType) || Constants.IO_CLOSEABLE.equals(rawType)) {
      closeable = true;
    } else {
      if (qualifierName == null) {
        final String mainType = GenericType.removeParameter(rawType);
        final String iShortName = Util.shortName(mainType);
        if (beanSimpleName.endsWith(iShortName)) {
          // derived qualifier name based on prefix to interface short name
          qualifierName = beanSimpleName.substring(0, beanSimpleName.length() - iShortName.length()).toLowerCase();
        }
      }
      // check if any unknown generic types are in the parameters (T,T2, etc.)
      final var knownType =
          GenericType.parse(rawType).params().stream()
              .flatMap(g -> Stream.concat(Stream.of(g), g.params().stream()))
              .noneMatch(g -> element(g.mainType()) == null);

      interfaceTypes.add(knownType ? rawType : GenericType.removeParameter(rawType));
      if (!rawType.startsWith("java.lang.")) {

        for (final TypeMirror supertype : types().directSupertypes(anInterface)) {
          readInterfacesOf(supertype);
        }
      }
    }
  }

  private boolean isPublic(Element element) {
    return element != null && element.getModifiers().contains(Modifier.PUBLIC);
  }
}
