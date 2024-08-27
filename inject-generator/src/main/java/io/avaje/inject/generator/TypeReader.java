package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toList;

final class TypeReader {

  private final boolean forBean;
  private final TypeElement beanType;
  private final ImportTypeMap importTypes;
  private final TypeExtendsReader extendsReader;
  private final TypeAnnotationReader annotationReader;
  private Set<UType> genericTypes;
  private String typesRegister;
  private final List<UType> injectsTypes;

  TypeReader(
      Optional<BeanTypesPrism> injectsTypes,
      UType genericType,
      TypeElement beanType,
      ImportTypeMap importTypes,
      boolean factory) {
    this(injectsTypes, genericType, true, beanType, importTypes, factory);
  }

  TypeReader(
      Optional<BeanTypesPrism> injectsTypes,
      UType genericType,
      TypeElement returnElement,
      ImportTypeMap importTypes) {
    this(injectsTypes, genericType, false, returnElement, importTypes, false);
  }

  private TypeReader(
      Optional<BeanTypesPrism> injectsTypes,
      UType genericType,
      boolean forBean,
      TypeElement beanType,
      ImportTypeMap importTypes,
      boolean factory) {
    this.injectsTypes =
      injectsTypes.map(BeanTypesPrism::value).stream()
        .flatMap(List::stream)
        .map(UType::parse)
        .collect(toList());
    this.forBean = forBean;
    this.beanType = beanType;
    this.importTypes = importTypes;
    final boolean proxyBean = forBean && ProxyPrism.isPresent(beanType);
    this.extendsReader = new TypeExtendsReader(genericType, beanType, factory, importTypes, proxyBean);
    this.annotationReader = new TypeAnnotationReader(beanType);
  }

  String typesRegister() {
    return typesRegister;
  }

  List<String> provides() {
    if (!injectsTypes.isEmpty()) {
      return injectsTypes.stream().map(UType::full).collect(toList());
    }
    return extendsReader.provides().stream().map(UType::full).collect(toList());
  }

  List<String> autoProvides() {
    if (!injectsTypes.isEmpty()) {
      return injectsTypes.stream().map(UType::full).collect(toList());
    }
    return extendsReader.autoProvides().stream()
      .filter(u -> u.componentTypes().stream().noneMatch(p -> p.kind() == TypeKind.TYPEVAR))
      .map(UType::full)
      .collect(toList());
  }

  String providesAspect() {
    return extendsReader.providesAspect();
  }

  boolean isClosable() {
    return extendsReader.isCloseable();
  }

  BeanAspects hasAspects() {
    return extendsReader.hasAspects();
  }

  List<FieldReader> injectFields() {
    return extendsReader.injectFields();
  }

  List<MethodReader> injectMethods() {
    return extendsReader.injectMethods();
  }

  List<MethodReader> factoryMethods() {
    return extendsReader.factoryMethods();
  }

  List<MethodReader> observerMethods() {
    return extendsReader.observerMethods();
  }

  Element postConstructMethod() {
    return extendsReader.postConstructMethod();
  }

  Element preDestroyMethod() {
    return extendsReader.preDestroyMethod();
  }

  Integer preDestroyPriority() {
    return extendsReader.preDestroyPriority();
  }

  MethodReader constructor() {
    return extendsReader.constructor();
  }

  Set<UType> genericTypes() {
    return genericTypes;
  }

  void process() {
    extendsReader.process(forBean);
    if (forBean) {
      annotationReader.process();
    }
    initRegistrationTypes();
  }

  String name() {
    NamedPrism named = NamedPrism.getInstanceOn(beanType);
    if (named != null) {
      return named.value().replace("\"", "\\\"");
    }
    if (annotationReader.hasQualifierName()) {
      return annotationReader.qualifierName();
    }
    return extendsReader.qualifierName();
  }

  private void initRegistrationTypes() {
    TypeAppender appender = new TypeAppender(importTypes);
    if (injectsTypes.isEmpty()) {
      appender.add(extendsReader.baseType());
      appender.add(extendsReader.provides());
    } else {
      appender.add(injectsTypes);
    }
    this.genericTypes = appender.genericTypes();
    this.typesRegister = appender.asString();
  }

  void extraImports(ImportTypeMap importTypes) {
    if (!genericTypes.isEmpty()) {
      importTypes.add(Constants.TYPE);
      importTypes.add(Constants.GENERICTYPE);
      genericTypes.forEach(t -> importTypes.addAll(t.importTypes()));
    }
  }
}
