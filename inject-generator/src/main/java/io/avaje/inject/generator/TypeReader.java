package io.avaje.inject.generator;

import javax.inject.Named;

import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TypeReader {

  private final boolean forBean;
  private final TypeElement beanType;
  private final Set<String> importTypes;
  private final TypeExtendsReader extendsReader;
  private final TypeInterfaceReader interfaceReader;
  private final TypeAnnotationReader annotationReader;
  private boolean beanLifeCycle;
  private String typesRegister;

  TypeReader(TypeElement beanType, ProcessingContext context, Set<String> importTypes) {
    this(true, beanType, context, importTypes);
  }

  TypeReader(TypeElement returnElement, ProcessingContext context) {
    this(false, returnElement, context, new LinkedHashSet<>());
  }

  private TypeReader(boolean forBean, TypeElement beanType, ProcessingContext context, Set<String> importTypes) {
    this.forBean = forBean;
    this.beanType = beanType;
    this.importTypes = importTypes;
    this.extendsReader = new TypeExtendsReader(beanType, context);
    this.interfaceReader = new TypeInterfaceReader(beanType, context);
    this.annotationReader = new TypeAnnotationReader(beanType, context);
  }

  boolean isBeanLifeCycle() {
    return beanLifeCycle;
  }

  String getTypesRegister() {
    return typesRegister;
  }

  List<String> getInterfaces() {
    return interfaceReader.getInterfaceTypes();
  }

  void process() {
    extendsReader.process();
    interfaceReader.process();
    beanLifeCycle = interfaceReader.isBeanLifeCycle();
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
    List<String> interfaceTypes = interfaceReader.getInterfaceTypes();
    if (interfaceTypes.isEmpty()) {
      // only register extends type if no interfaces implemented
      appender.add(extendsReader.getExtendsTypes());
    } else {
      String baseType = extendsReader.getBaseType();
      if (!interfaceTypes.contains(baseType)) {
        appender.add(baseType);
      }
      appender.add(interfaceTypes);
    }
    if (forBean) {
      appender.add(annotationReader.getAnnotationTypes());
    }
    this.typesRegister = appender.asString();
  }

  void addImports(Set<String> importTypes) {
    importTypes.addAll(this.importTypes);
  }
}
