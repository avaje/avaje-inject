package io.avaje.inject.generator;

import io.avaje.inject.Primary;
import io.avaje.inject.Secondary;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

class BeanReader {

  private final ProcessingContext context;
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
  private final boolean primary;
  private final boolean secondary;
  private final boolean requestScopedBean;

  private boolean writtenToFile;

  BeanReader(TypeElement beanType, ProcessingContext context, boolean factory) {
    this.beanType = beanType;
    this.context = context;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.primary = (beanType.getAnnotation(Primary.class) != null);
    this.secondary = !primary && (beanType.getAnnotation(Secondary.class) != null);
    this.typeReader = new TypeReader(beanType, context, importTypes, factory);

    typeReader.process();
    this.requestScopedBean = typeReader.isRequestScopeBean();
    this.requestParams = new BeanRequestParams(context, type, requestScopedBean);
    this.name = typeReader.getName();
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

  BeanReader read() {
    if (constructor != null) {
      constructor.addImports(importTypes);
      constructor.checkRequest(requestParams);
    }
    for (FieldReader fields : injectFields) {
      fields.addImports(importTypes);
      fields.checkRequest(requestParams);
    }
    for (MethodReader methods : injectMethods) {
      methods.addImports(importTypes);
      methods.checkRequest(requestParams);
    }
    for (MethodReader factoryMethod : factoryMethods) {
      factoryMethod.addImports(importTypes);
    }
    if (requestScopedBean) {
      importTypes.add(Constants.REQUESTSCOPEPROVIDER);
      importTypes.add(Constants.REQUESTSCOPE);
    }
    return this;
  }

  List<String> getDependsOn() {
    List<String> list = new ArrayList<>();
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

  List<String> getInterfaces() {
    return typeReader.getInterfaces();
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
    return !requestScopedBean && (postConstructMethod != null || preDestroyMethod != null || typeReader.isClosable());
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
    writer.append("      ");
    if (isExtraInjectionRequired() || hasLifecycleMethods()) {
      writer.append("%s $bean = ", shortName);
    }
    String flags = primary ? "Primary" : secondary ? "Secondary" : "";
    writer.append("builder.register%s(bean);", flags).eol();
  }

  void addLifecycleCallbacks(Append writer) {
    if (postConstructMethod != null) {
      writer.append("      builder.addPostConstruct($bean::%s);", postConstructMethod.getSimpleName()).eol();
    }
    if (preDestroyMethod != null) {
      writer.append("      builder.addPreDestroy($bean::%s);", preDestroyMethod.getSimpleName()).eol();
    } else if (typeReader.isClosable()) {
      writer.append("      builder.addPreDestroy($bean);").eol();
    }
  }

  private Set<String> importTypes() {
    importTypes.add(Constants.GENERATED);
    importTypes.add(Constants.BUILDER);
    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    requestParams.addImports(importTypes);
    return importTypes;
  }

  void writeImports(Append writer) {
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

  boolean isRequestScopedBean() {
    return requestScopedBean;
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

  void buildReq(Append writer) {
    writer.append("    builder.requestScope(%s.class, new RequestScopeProvider<%s>() {", shortName, shortName).eol();
    writer.append("      @Override").eol();
    writer.append("      public %s provide(RequestScope scope) {", shortName).eol();
  }

  void buildReqEnd(Append writer) {
    writer.append("        return bean;").eol();
    writer.append("      }").eol();
    writer.append("    }");

    final String ifaceTypes = typeReader.getTypesRegister();
    if (ifaceTypes != null) {
      if (name != null && !name.isEmpty()) {
        writer.append(", \"%s\"", name);
        writer.append(", ");
        writer.append(ifaceTypes);
      }
    }
    writer.append(");").eol();
  }

  void writePostConstruct(Append writer) {
    if (postConstructMethod != null) {
      writer.append("        bean.%s();", postConstructMethod.getSimpleName()).eol();
    }
  }

  void writePreDestroy(Append writer) {
    if (preDestroyMethod != null) {
      writer.append("        scope.addClosable(bean::%s);", preDestroyMethod.getSimpleName()).eol();
    } else if (typeReader.isClosable()) {
      writer.append("        scope.addClosable(bean);").eol();
    }
  }

  List<FieldReader> getInjectFields() {
    return typeReader.getInjectFields();
  }

  List<MethodReader> getInjectMethods() {
    return typeReader.getInjectMethods();
  }
}
