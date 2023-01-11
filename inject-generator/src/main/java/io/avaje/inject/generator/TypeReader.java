package io.avaje.inject.generator;

import jakarta.inject.Named;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

final class TypeReader {

  private final boolean forBean;
  private final TypeElement beanType;
  private final ImportTypeMap importTypes;
  private final TypeExtendsReader extendsReader;
  private final TypeAnnotationReader annotationReader;
  private Set<GenericType> genericTypes;
  private String typesRegister;

  TypeReader(GenericType genericType, TypeElement beanType, ProcessingContext context, ImportTypeMap importTypes, boolean factory) {
    this(genericType, true, beanType, context, importTypes, factory);
  }

  TypeReader(GenericType genericType, TypeElement returnElement, ProcessingContext context, ImportTypeMap importTypes) {
    this(genericType, false, returnElement, context, importTypes, false);
  }

  private TypeReader(GenericType genericType, boolean forBean, TypeElement beanType, ProcessingContext context, ImportTypeMap importTypes, boolean factory) {
    this.forBean = forBean;
    this.beanType = beanType;
    this.importTypes = importTypes;
    this.extendsReader = new TypeExtendsReader(genericType, beanType, context, factory, importTypes);
    this.annotationReader = new TypeAnnotationReader(beanType, context);
  }

  String getTypesRegister() {
    return typesRegister;
  }

  List<String> getProvides() {
    return extendsReader.getProvides();
  }

  boolean isClosable() {
    return extendsReader.isCloseable();
  }

  BeanAspects hasAspects() {
    return extendsReader.hasAspects();
  }

  List<FieldReader> getInjectFields() {
    return extendsReader.getInjectFields();
  }

  List<MethodReader> getInjectMethods() {
    return extendsReader.getInjectMethods();
  }

  List<MethodReader> getFactoryMethods() {
    return extendsReader.getFactoryMethods();
  }

  Element getPostConstructMethod() {
    return extendsReader.getPostConstructMethod();
  }

  Element getPreDestroyMethod() {
    return extendsReader.getPreDestroyMethod();
  }

  MethodReader getConstructor() {
    return extendsReader.getConstructor();
  }

  Set<GenericType> getGenericTypes() {
    return genericTypes;
  }

  void process() {
    extendsReader.process(forBean);
    if (forBean) {
      annotationReader.process();
    }
    initRegistrationTypes();
  }

  String getName() {
    Named named = beanType.getAnnotation(Named.class);
    if (named != null) {
      return named.value().toLowerCase();
    }
    if (annotationReader.hasQualifierName()) {
      return annotationReader.getQualifierName();
    }
    return extendsReader.getQualifierName();
  }

  private void initRegistrationTypes() {
    TypeAppender appender = new TypeAppender(importTypes);
    appender.add(extendsReader.getBaseType());
    appender.add(extendsReader.getProvides());
    if (forBean) {
      appender.add(annotationReader.getAnnotationTypes());
    }
    this.genericTypes = appender.genericTypes();
    this.typesRegister = appender.asString();
  }

  void extraImports(ImportTypeMap importTypes) {
    if (!genericTypes.isEmpty()) {
      importTypes.add(Constants.TYPE);
      importTypes.add(Constants.GENERICTYPE);
      // TYPE_ generic types are fully qualified
    }
  }
}
