package io.avaje.inject.generator;

import javax.inject.Named;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TypeReader {

  private final boolean forBean;
  private final TypeElement beanType;
  private final Set<String> importTypes;
  private final TypeExtendsReader extendsReader;
  private final TypeAnnotationReader annotationReader;
  private Set<GenericType> genericTypes;
  private String typesRegister;

  TypeReader(TypeElement beanType, ProcessingContext context, Set<String> importTypes, boolean factory) {
    this(true, beanType, context, importTypes, factory);
  }

  TypeReader(TypeElement returnElement, ProcessingContext context) {
    this(false, returnElement, context, new LinkedHashSet<>(), false);
  }

  private TypeReader(boolean forBean, TypeElement beanType, ProcessingContext context, Set<String> importTypes, boolean factory) {
    this.forBean = forBean;
    this.beanType = beanType;
    this.importTypes = importTypes;
    this.extendsReader = new TypeExtendsReader(beanType, context, factory);
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

  void addImports(Set<String> importTypes) {
    importTypes.addAll(this.importTypes);
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
    appender.add(extendsReader.getExtendsTypes());
    appender.add(extendsReader.getInterfaceTypes());
    if (forBean) {
      appender.add(annotationReader.getAnnotationTypes());
    }
    this.genericTypes = appender.genericTypes();
    this.typesRegister = appender.asString();
  }

  void extraImports(Set<String> importTypes) {
    if (!genericTypes.isEmpty()) {
      importTypes.add(Constants.TYPE);
      importTypes.add(Constants.GENERICTYPE);
      importTypes.add(Constants.PROVIDER);
      for (GenericType type : genericTypes) {
        type.addImports(importTypes);
      }
    }
  }
}
