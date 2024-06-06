package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logWarn;
import static io.avaje.inject.generator.APContext.types;
import static io.avaje.inject.generator.ProcessingContext.asElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Read the inheritance types for a given bean type.
 */
final class TypeExtendsReader {

  private static final Set<String> ROUTE_TYPES = Set.of(
    "io.avaje.http.api.AvajeJavalinPlugin",
    "io.helidon.webserver.http.HttpFeature");

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private static final String JAVA_LANG_RECORD = "java.lang.Record";
  private final UType baseUType;
  private final TypeElement baseType;
  private final TypeExtendsInjection extendsInjection;
  private final List<UType> extendsTypes = new ArrayList<>();
  private final List<UType> interfaceTypes = new ArrayList<>();
  private final List<UType> providesTypes = new ArrayList<>();
  private final String beanSimpleName;
  private final boolean baseTypeIsInterface;
  private final boolean publicAccess;
  private final boolean autoProvide;
  private final boolean proxyBean;
  private final boolean controller;
  private boolean closeable;
  /**
   * The implied qualifier name based on naming convention.
   */
  private String qualifierName;
  private String providesAspect = "";

  TypeExtendsReader(UType baseUType, TypeElement baseType, boolean factory, ImportTypeMap importTypes, boolean proxyBean) {
    this.baseUType = baseUType;
    this.baseType = baseType;
    this.extendsInjection = new TypeExtendsInjection(baseType, factory, importTypes);
    this.beanSimpleName = baseType.getSimpleName().toString();

    this.baseTypeIsInterface = baseType.getKind() == ElementKind.INTERFACE;
    this.publicAccess = baseType.getModifiers().contains(Modifier.PUBLIC);
    this.proxyBean = proxyBean;
    this.controller = hasAnnotation(Constants.CONTROLLER);
    this.autoProvide = autoProvide();
  }

  private boolean autoProvide() {
    return publicAccess
      && !controller
      && !FactoryPrism.isPresent(baseType)
      && !ProxyPrism.isPresent(baseType);
  }

  private boolean hasAnnotation(String annotationFQN) {
    for (final var m : baseType.getAnnotationMirrors()) {
      final CharSequence mfqn = ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName();
      if (annotationFQN.contentEquals(mfqn)) {
        return true;
      }
    }
    return false;
  }

  UType baseType() {
    return baseUType;
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

  List<MethodReader> observerMethods() {
    return extendsInjection.observerMethods();
  }

  Element postConstructMethod() {
    return extendsInjection.postConstructMethod();
  }

  Element preDestroyMethod() {
    return extendsInjection.preDestroyMethod();
  }

  Integer preDestroyPriority() {
    return extendsInjection.preDestroyPriority();
  }

  MethodReader constructor() {
    return extendsInjection.constructor();
  }

  String providesAspect() {
    return providesAspect;
  }

  List<UType> autoProvides() {
    if (controller || implementsBeanFactory()) {
      // http controller, or request scoped controller via BeanFactory
      return List.of();
    }
    if (hasAnnotation(Constants.HTTP_GENERATED)) {
      // http route
      return providesTypes.stream()
        .filter(ut -> ROUTE_TYPES.contains(ut.mainType()))
        .collect(Collectors.toList());
    }
    if (baseTypeIsInterface) {
      return List.of(Util.unwrapProvider(baseUType));
    }
    var autoProvides = new ArrayList<>(interfaceTypes);
    autoProvides.addAll(extendsTypes);
    if (!autoProvide || !providesAspect.isEmpty()) {
      autoProvides.remove(baseUType);
    } else {
      autoProvides.add(Util.unwrapProvider(baseUType));
    }
    return autoProvides;
  }

  private boolean implementsBeanFactory() {
    for (UType interfaceType : interfaceTypes) {
      if (Constants.BEAN_FACTORY.equals(interfaceType.mainType())) {
        return true;
      }
    }
    return false;
  }

  List<UType> provides() {
    return providesTypes;
  }

  boolean isCloseable() {
    return closeable;
  }

  void process(boolean forBean) {
    extendsTypes.add(baseUType);
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
          qualifierName = baseName.substring(0, baseName.length() - superName.length());
        }
      }
      addSuperType(superElement, superMirror, proxyBean);
    }

    providesTypes.addAll(extendsTypes);
    providesTypes.addAll(interfaceTypes);
    providesTypes.remove(baseUType);
    // we can't provide a type that is getting injected
    extendsInjection.removeFromProvides(providesTypes);
    providesAspect = initProvidesAspect();
  }

  private String initProvidesAspect() {
    for (final var type : providesTypes) {
      var providesType = type.full();
      if (Util.isAspectProvider(providesType)) {
        return Util.extractAspectType(providesType);
      }
    }
    return "";
  }

  private void addSuperType(TypeElement element, TypeMirror mirror, boolean proxyBean) {
    readInterfaces(element);
    final String fullName = mirror.toString();
    if (!JAVA_LANG_OBJECT.equals(fullName) && !JAVA_LANG_RECORD.equals(fullName)) {
      final String type = Util.unwrapProvider(fullName);

      if (proxyBean || isPublic(element)) {
        final var genericType = !Objects.equals(fullName, type) ? UType.parse(mirror).param0() : UType.parse(mirror);
        // check if any unknown generic types are in the parameters (T,T2, etc.)
        final var knownType = genericType.componentTypes().stream()
          .flatMap(g -> Stream.concat(Stream.of(g), g.componentTypes().stream()))
          .noneMatch(g -> g.kind() == TypeKind.TYPEVAR);

        extendsTypes.add(knownType ? Util.unwrapProvider(mirror) : genericType);
        extendsInjection.read(element);
      }

      final var superMirror = element.getSuperclass();
      final var superElement = asElement(superMirror);
      if (superElement != null) {
        addSuperType(superElement, superMirror, false);
      }
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
	  final UType rawUType = Util.unwrapProvider(anInterface);
	    if (JAVA_LANG_OBJECT.equals(rawType)) {
      // we can stop
      return;
    }
    if (rawType.indexOf('.') == -1) {
      logWarn("skip when no package on interface " + rawType);
    } else if (Constants.AUTO_CLOSEABLE.equals(rawType) || Constants.IO_CLOSEABLE.equals(rawType)) {
      closeable = true;
    } else {
      if (qualifierName == null) {
        final String mainType = rawUType.mainType();
        final String iShortName = Util.shortName(mainType);
        if (beanSimpleName.endsWith(iShortName)) {
          // derived qualifier name based on prefix to interface short name
          qualifierName = beanSimpleName.substring(0, beanSimpleName.length() - iShortName.length());
        }
      }
      interfaceTypes.add(rawUType);
      if (Util.notJavaLang(rawType)) {
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
