package io.avaje.inject.generator;

import io.avaje.inject.Bean;
import io.avaje.inject.Primary;
import io.avaje.inject.Secondary;
import javax.inject.Inject;
import javax.inject.Named;

import javax.lang.model.element.*;
import java.util.*;

class BeanReader {

  private final TypeElement beanType;

  private final ProcessingContext context;

  private final String shortName;

  private final String type;
  private String name;

  private MethodReader injectConstructor;
  private final List<MethodReader> otherConstructors = new ArrayList<>();
  private final List<MethodReader> factoryMethods = new ArrayList<>();

  private Element postConstructMethod;
  private Element preDestroyMethod;

  private final List<FieldReader> injectFields = new ArrayList<>();
  private final List<MethodReader> injectMethods = new ArrayList<>();
  private final Set<String> importTypes = new TreeSet<>();
  private final BeanRequestParams requestParams;
  private final TypeReader typeReader;
  private MethodReader constructor;
  private boolean writtenToFile;
  private boolean primary;
  private boolean secondary;
  private boolean requestScopedBean;

  BeanReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.context = context;
    this.requestParams = new BeanRequestParams(type);
    this.typeReader = new TypeReader(beanType, context, importTypes);
    init();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  private void init() {
    typeReader.process();
    requestScopedBean = typeReader.isRequestScopeBean();
    name = typeReader.getName();
    primary = (beanType.getAnnotation(Primary.class) != null);
    secondary = !primary && (beanType.getAnnotation(Secondary.class) != null);
  }

  TypeElement getBeanType() {
    return beanType;
  }

  Element getPostConstructMethod() {
    return postConstructMethod;
  }

  Element getPreDestroyMethod() {
    return preDestroyMethod;
  }

  List<FieldReader> getInjectFields() {
    return injectFields;
  }

  List<MethodReader> getInjectMethods() {
    return injectMethods;
  }

  void read(boolean factory) {
    for (Element element : beanType.getEnclosedElements()) {
      ElementKind kind = element.getKind();
      switch (kind) {
        case CONSTRUCTOR:
          readConstructor(element);
          break;
        case FIELD:
          readField(element);
          break;
        case METHOD:
          readMethod(element, factory);
          break;
      }
    }
    constructor = findConstructor();
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
  }

  private MethodReader findConstructor() {
    if (injectConstructor != null) {
      return injectConstructor;
    }
    if (otherConstructors.size() == 1) {
      return otherConstructors.get(0);
    }
    // check if there is only one public constructor
    List<MethodReader> allPublic = new ArrayList<>();
    for (MethodReader ctor : otherConstructors) {
      if (ctor.isPublic()) {
        allPublic.add(ctor);
      }
    }
    if (allPublic.size() == 1) {
      // fallback to the single public constructor
      return allPublic.get(0);
    }
    return null;
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

  private void readConstructor(Element element) {
    ExecutableElement ex = (ExecutableElement) element;

    MethodReader methodReader = new MethodReader(context, ex, beanType);
    methodReader.read();
    Inject inject = element.getAnnotation(Inject.class);
    if (inject != null) {
      injectConstructor = methodReader;
    } else {
      if (methodReader.isNotPrivate()) {
        otherConstructors.add(methodReader);
      }
    }
  }

  private void readField(Element element) {
    Inject inject = element.getAnnotation(Inject.class);
    if (inject != null) {
      injectFields.add(new FieldReader(element));
    }
  }

  private void readMethod(Element element, boolean factory) {
    ExecutableElement methodElement = (ExecutableElement) element;
    if (factory) {
      Bean bean = element.getAnnotation(Bean.class);
      if (bean != null) {
        addFactoryMethod(methodElement, bean);
      }
    }
    Inject inject = element.getAnnotation(Inject.class);
    if (inject != null) {
      MethodReader methodReader = new MethodReader(context, methodElement, beanType);
      if (methodReader.isNotPrivate()) {
        methodReader.read();
        injectMethods.add(methodReader);
      }
    }
    if (AnnotationUtil.hasAnnotationWithName(element, "PostConstruct")) {
      postConstructMethod = element;
    }
    if (AnnotationUtil.hasAnnotationWithName(element, "PreDestroy")) {
      preDestroyMethod = element;
    }
  }

  /**
   * Return the short name of the element.
   */
  private String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  private void addFactoryMethod(ExecutableElement methodElement, Bean bean) {
    // Not yet reading Qualifier annotations, Named only at this stage
    Named named = methodElement.getAnnotation(Named.class);
    MethodReader methodReader = new MethodReader(context, methodElement, beanType, bean, named);
    methodReader.read();
    factoryMethods.add(methodReader);
  }

  /**
   * Return the key for meta data (type and name)
   */
  public String getMetaKey() {
    if (name != null) {
      return type + ":" + name;
    }
    return type;
  }

  /**
   * Return true if lifecycle via annotated methods is required.
   */
  boolean hasLifecycleMethods() {
    return !requestScopedBean && (postConstructMethod != null || preDestroyMethod != null);
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
      writer.append("      builder.addPreDestroy(bean);").eol();
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
    return isRequestScopedController() ? "$factory" : "$di";
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
}
