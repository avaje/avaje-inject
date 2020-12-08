package io.avaje.inject.generator;

import io.avaje.inject.Bean;
import io.avaje.inject.Primary;
import io.avaje.inject.Secondary;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

  private final List<String> interfaceTypes = new ArrayList<>();

  private String addForType;

  private final Set<String> importTypes = new TreeSet<>();

  private final BeanRequestParams requestParams;

  private MethodReader constructor;

  private String registrationTypes;

  private boolean writtenToFile;

  /**
   * Set to true when the bean directly implements BeanLifecycle.
   */
  private boolean beanLifeCycle;
  private boolean primary;
  private boolean secondary;

  BeanReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.context = context;
    this.requestParams = new BeanRequestParams(type);
    init();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  private void init() {
    StringBuilder sb = new StringBuilder();

    for (TypeMirror anInterface : beanType.getInterfaces()) {
      String type = Util.unwrapProvider(anInterface.toString());
      if (Constants.isBeanLifecycle(type)) {
        beanLifeCycle = true;
      } else if (type.indexOf('.') == -1) {
        context.logWarn("skip when no package on interface " + type);
      } else {
        interfaceTypes.add(type);
        if (!GenericType.isGeneric(type)) {
          importTypes.add(type);
          sb.append(", ").append(Util.shortName(type)).append(".class");
        }
      }
    }
    if (interfaceTypes.size() == 1) {
      String ifaceType = interfaceTypes.get(0);
      if (!GenericType.isGeneric(ifaceType)) {
        // only register for non-generic interfaces for the moment
        addForType = Util.addForInterface(ifaceType);
      }
    }

    // get class level annotations (that are not Named and Singleton)
    for (AnnotationMirror annotationMirror : beanType.getAnnotationMirrors()) {

      DeclaredType annotationType = annotationMirror.getAnnotationType();
      Qualifier qualifier = annotationType.asElement().getAnnotation(Qualifier.class);
      String annType = annotationType.toString();
      if (qualifier != null) {
        this.name = Util.shortName(annType);
      } else if (annType.indexOf('.') == -1) {
        context.logWarn("skip when no package on annotation " + annType);
      } else {
        if (IncludeAnnotations.include(annType)) {
          importTypes.add(annType);
          sb.append(", ").append(Util.shortName(annType)).append(".class");
        }
      }
    }
    Named named = beanType.getAnnotation(Named.class);
    if (named != null) {
      this.name = named.value();
    }

    primary = (beanType.getAnnotation(Primary.class) != null);
    secondary = !primary && (beanType.getAnnotation(Secondary.class) != null);
    registrationTypes = sb.toString();
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
      fields.checkRequest(requestParams);
    }
    for (MethodReader methods : injectMethods) {
      methods.addImports(importTypes);
      methods.checkRequest(requestParams);
    }
    for (MethodReader factoryMethod : factoryMethods) {
      factoryMethod.addImports(importTypes);
    }
  }

  private MethodReader findConstructor() {
    if (injectConstructor != null) {
      return injectConstructor;
    }
    if (otherConstructors.size() == 1) {
      return otherConstructors.get(0);
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
    return interfaceTypes;
  }

  /**
   * Return all the interfaces and annotations associated with this bean.
   * <p>
   * The bean is made a 'member' of the list of beans that implement the interface or have the
   * annotation.
   * </p>
   */
  String getInterfacesAndAnnotations() {
    return registrationTypes;
  }

  private void readConstructor(Element element) {

    ExecutableElement ex = (ExecutableElement) element;

    MethodReader methodReader = new MethodReader(context, ex, beanType);
    methodReader.read();

    Inject inject = element.getAnnotation(Inject.class);
    if (inject != null) {
      injectConstructor = methodReader;
    } else {
      otherConstructors.add(methodReader);
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
      methodReader.read();
      if (!methodReader.getParams().isEmpty()) {
        injectMethods.add(methodReader);
      }
    }

    if (hasAnnotationWithName(element, "PostConstruct")) {
      postConstructMethod = element;
    }

    if (hasAnnotationWithName(element, "PreDestroy")) {
      preDestroyMethod = element;
    }
  }

  /**
   * Return true if the element has an annotation with the matching short name.
   */
  private boolean hasAnnotationWithName(Element element, String matchShortName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (matchShortName.equals(shortName(mirror.getAnnotationType().asElement()))) {
        return true;
      }
    }
    return false;
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

  boolean isLifecycleRequired() {
    return beanLifeCycle || isLifecycleWrapperRequired();
  }

  /**
   * Return true if lifecycle via annotated methods is required.
   */
  boolean isLifecycleWrapperRequired() {
    return postConstructMethod != null || preDestroyMethod != null;
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
    if (addForType != null) {
      writer.append(addForType).append(".class, ");
    }
    writer.append(shortName).append(".class)) {").eol();
  }

  void buildRegister(Append writer) {
    writer.append("      ");
    if (isExtraInjectionRequired() || isLifecycleRequired()) {
      writer.append("%s $bean = ", shortName);
    }
    String flags = primary ? "Primary" : secondary ? "Secondary" : "";
    writer.append("builder.register%s(bean, ", flags);
    if (name == null) {
      writer.append("null");
    } else {
      writer.append("\"%s\"", name);
    }
    // add interfaces and annotations
    writer.append(getInterfacesAndAnnotations()).append(");").eol();
  }

  void buildAddLifecycle(Append writer) {
    writer.append("      builder.addLifecycle(");
    if (beanLifeCycle) {
      writer.append("$bean");
    } else {
      writer.append("new %s$di($bean)", shortName);
    }
    writer.append(");").eol();
  }

  private Set<String> importTypes() {
    if (isLifecycleWrapperRequired()) {
      importTypes.add(Constants.BEAN_LIFECYCLE);
    }
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
  boolean isRequestScoped() {
    return requestParams.isRequestScoped();
  }

  String suffix() {
    return isRequestScoped() ? "$factory" : "$di";
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

}
