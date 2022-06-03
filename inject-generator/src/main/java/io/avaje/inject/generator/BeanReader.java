package io.avaje.inject.generator;

import io.avaje.inject.Primary;
import io.avaje.inject.Prototype;
import io.avaje.inject.Secondary;
import io.avaje.inject.spi.Proxy;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

class BeanReader {

  private final TypeElement beanType;
  private final String shortName;
  private final String type;
  private final String name;

  private final MethodReader constructor;
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final List<MethodReader> factoryMethods;
  private final Element postConstructMethod;
  private final Element preDestroyMethod;

  private final Set<String> importTypes = new TreeSet<>();
  private final BeanRequestParams requestParams;
  private final TypeReader typeReader;
  private final boolean prototype;
  private final boolean primary;
  private final boolean secondary;
  private final boolean proxy;
  private final BeanAspects aspects;
  private boolean writtenToFile;
  private boolean suppressBuilderImport;
  private boolean suppressGeneratedImport;
  private Set<GenericType> allGenericTypes;

  BeanReader(TypeElement beanType, ProcessingContext context, boolean factory) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.prototype = (beanType.getAnnotation(Prototype.class) != null);
    this.primary = (beanType.getAnnotation(Primary.class) != null);
    this.secondary = !primary && (beanType.getAnnotation(Secondary.class) != null);
    this.proxy = (beanType.getAnnotation(Proxy.class) != null);
    this.typeReader = new TypeReader(beanType, context, importTypes, factory);

    typeReader.process();
    this.requestParams = new BeanRequestParams(type);
    this.name = typeReader.getName();
    this.aspects = typeReader.hasAspects();
    this.injectMethods = typeReader.getInjectMethods();
    this.injectFields = typeReader.getInjectFields();
    this.factoryMethods = typeReader.getFactoryMethods();
    this.postConstructMethod = typeReader.getPostConstructMethod();
    this.preDestroyMethod = typeReader.getPreDestroyMethod();
    this.constructor = typeReader.getConstructor();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement getBeanType() {
    return beanType;
  }

  BeanAspects aspects() {
    return aspects;
  }

  boolean prototype() {
    return prototype;
  }

  BeanReader read() {
    if (constructor != null) {
      constructor.addImports(importTypes);
      constructor.checkRequest(requestParams);
    }
    for (FieldReader field : injectFields) {
      field.addImports(importTypes);
      field.checkRequest(requestParams);
    }
    for (MethodReader method : injectMethods) {
      method.addImports(importTypes);
      method.checkRequest(requestParams);
    }
    for (MethodReader factoryMethod : factoryMethods) {
      factoryMethod.addImports(importTypes);
    }
    return this;
  }

  List<Dependency> getDependsOn() {
    List<Dependency> list = new ArrayList<>();
    if (constructor != null) {
      for (MethodReader.MethodParam param : constructor.getParams()) {
        list.add(param.getDependsOn());
      }
    }
    return list;
  }

  List<MethodReader> getFactoryMethods() {
    return factoryMethods;
  }

  List<String> getProvides() {
    return typeReader.getProvides();
  }

  Set<GenericType> getGenericTypes() {
    return typeReader.getGenericTypes();
  }

  Set<GenericType> allGenericTypes() {
    if (allGenericTypes != null) {
      return allGenericTypes;
    }
    allGenericTypes = new LinkedHashSet<>(typeReader.getGenericTypes());
    for (FieldReader field : injectFields) {
      field.addDependsOnGeneric(allGenericTypes);
    }
    for (MethodReader method : injectMethods) {
      method.addDependsOnGeneric(allGenericTypes);
    }
    if (constructor != null) {
      constructor.addDependsOnGeneric(allGenericTypes);
    }
    for (MethodReader factoryMethod : getFactoryMethods()) {
      allGenericTypes.addAll(factoryMethod.getGenericTypes());
    }
    return allGenericTypes;
  }

  /**
   * Return the short name of the element.
   */
  private String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  /**
   * Return the key for meta data (type and name)
   */
  String getMetaKey() {
    if (name != null) {
      return type + ":" + name;
    }
    return type;
  }

  /**
   * Return true if lifecycle via annotated methods is required.
   */
  boolean hasLifecycleMethods() {
    return (postConstructMethod != null || preDestroyMethod != null || typeReader.isClosable());
  }

  List<MetaData> createFactoryMethodMeta() {
    if (factoryMethods.isEmpty()) {
      return Collections.emptyList();
    }
    List<MetaData> metaList = new ArrayList<>(factoryMethods.size());
    for (MethodReader factoryMethod : factoryMethods) {
      metaList.add(factoryMethod.createMeta());
    }
    return metaList;
  }

  MetaData createMeta() {
    String type;
    if (beanType.getNestingKind().isNested()) {
      type = beanType.getEnclosingElement().toString() + "$" + beanType.getSimpleName();
    } else {
      type = beanType.getQualifiedName().toString();
    }
    MetaData metaData = new MetaData(type, name);
    metaData.update(this);
    return metaData;
  }

  boolean isExtraInjectionRequired() {
    return !injectFields.isEmpty() || !injectMethods.isEmpty();
  }

  void buildAddFor(Append writer) {
    writer.append("    if (builder.isAddBeanFor(");
    if (name != null && !name.isEmpty()) {
      writer.append("\"%s\", ", name);
    }
    writer.append(typeReader.getTypesRegister());
    writer.append(")) {").eol();
  }

  void buildRegister(Append writer) {
    if (prototype) {
      return;
    }
    writer.append("      ");
    if (isExtraInjectionRequired() || hasLifecycleMethods()) {
      writer.append("%s $bean = ", shortName);
    }
    String flags = primary ? "Primary" : secondary ? "Secondary" : "";
    writer.append("builder.register%s(bean);", flags).eol();
  }

  void addLifecycleCallbacks(Append writer, String indent) {
    if (postConstructMethod != null && !prototype) {
      writer.append("%s builder.addPostConstruct($bean::%s);", indent, postConstructMethod.getSimpleName()).eol();
    }
    if (preDestroyMethod != null) {
      prototypeNotSupported(writer, "@PreDestroy");
      writer.append("%s builder.addPreDestroy($bean::%s);", indent, preDestroyMethod.getSimpleName()).eol();
    } else if (typeReader.isClosable() && !prototype) {
      writer.append("%s builder.addPreDestroy($bean);", indent).eol();
    }
  }

  void prototypePostConstruct(Append writer, String indent) {
    if (postConstructMethod != null) {
      writer.append("%s bean.%s();", indent, postConstructMethod.getSimpleName()).eol();
    }
  }

  private void prototypeNotSupported(Append writer, String lifecycle) {
    if (prototype) {
      writer.append("        // @Prototype scoped bean does not support %s lifecycle method", lifecycle).eol();
    }
  }

  private Set<String> importTypes() {
    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    typeReader.extraImports(importTypes);
    requestParams.addImports(importTypes);
    aspects.extraImports(importTypes);

    for (MethodReader factoryMethod : factoryMethods) {
      Set<GenericType> genericTypes = factoryMethod.getGenericTypes();
      if (!genericTypes.isEmpty()) {
        importTypes.add(Constants.TYPE);
        importTypes.add(Constants.GENERICTYPE);
        importTypes.add(Constants.PROVIDER);
        for (GenericType genericType : genericTypes) {
          genericType.addImports(importTypes);
        }
      }
    }
    checkImports();
    if (!suppressGeneratedImport){
      importTypes.add(Constants.GENERATED);
    }
    if (!suppressBuilderImport) {
      importTypes.add(Constants.BUILDER);
    }
    return importTypes;
  }

  private void checkImports() {
    for (String type : importTypes) {
      if (type.endsWith(".Builder")) {
        suppressBuilderImport = true;
      } else if (type.endsWith(".Generated")) {
        suppressGeneratedImport = true;
      }
    }
  }

  String builderType() {
    return suppressBuilderImport ? Constants.BUILDER : "Builder";
  }

  String generatedType() {
    return suppressGeneratedImport ? "@io.avaje.inject.spi.Generated" : "@Generated";
  }

  void writeImports(Append writer) {
    if (!allGenericTypes().isEmpty()) {
      importTypes.add(Constants.TYPE);
      importTypes.add(Constants.GENERICTYPE);
    }
    for (String importType : importTypes()) {
      if (Util.validImportType(importType)) {
        writer.append("import %s;", importType).eol();
      }
    }
    writer.eol();
  }

  MethodReader getConstructor() {
    return constructor;
  }

  boolean isWrittenToFile() {
    return writtenToFile;
  }

  void setWrittenToFile() {
    this.writtenToFile = true;
  }

  /**
   * Return true if the bean has a dependency which is a request scoped type.
   * Like Javalin Context, Helidon request and response types.
   * <p>
   * If request scoped then generate a BeanFactory instead.
   */
  boolean isRequestScopedController() {
    return requestParams.isRequestScopedController();
  }

  String suffix() {
    return isRequestScopedController() ? Constants.FACTORY : Constants.DI;
  }

  /**
   * Add interface for this as a BeanFactory (request scoped).
   */
  void factoryInterface(Append writer) {
    requestParams.factoryInterface(writer);
  }

  /**
   * Generate the BeanFactory dependencies and create method implementation.
   */
  void writeRequestCreate(Append writer) {
    if (constructor != null) {
      constructor.writeRequestDependency(writer);
    }
    for (FieldReader field : injectFields) {
      field.writeRequestDependency(writer);
    }
    for (MethodReader method : injectMethods) {
      method.writeRequestDependency(writer);
    }
    requestParams.writeRequestCreate(writer);
    writer.resetNextName();
    writer.append("    %s bean = new %s(", shortName, shortName);
    if (constructor != null) {
      constructor.writeRequestConstructor(writer);
    }
    writer.append(");").eol();
    for (FieldReader field : injectFields) {
      field.writeRequestInject(writer);
    }
    for (MethodReader method : injectMethods) {
      writer.append("    bean.%s(", method.getName());
      method.writeRequestConstructor(writer);
      writer.append(");").eol();
    }
    writer.append("    return bean;").eol();
    writer.append("  }").eol();
  }

  List<FieldReader> getInjectFields() {
    return typeReader.getInjectFields();
  }

  List<MethodReader> getInjectMethods() {
    return typeReader.getInjectMethods();
  }

  boolean isGenerateProxy() {
    return aspects.hasAspects() && !proxy;
  }

  void writeConstructorParams(Append writer) {
    if (constructor != null) {
      constructor.writeConstructorParams(writer);
    }
  }

  void writeConstructorInit(Append writer) {
    if (constructor != null) {
      writer.append("    super(");
      constructor.writeConstructorInit(writer);
      writer.append(");").eol();
    }
  }
}
