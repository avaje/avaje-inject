package io.avaje.inject.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.avaje.inject.generator.MethodReader.MethodParam;

final class AssistBeanReader {

  private final TypeElement beanType;
  private final String shortName;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final List<Element> assistedElements = new ArrayList<>();

  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final BeanRequestParams requestParams;
  private final TypeReader typeReader;
  private boolean writtenToFile;
  private boolean suppressBuilderImport;
  private boolean suppressGeneratedImport;

  AssistBeanReader(TypeElement beanType) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.typeReader = new TypeReader(GenericType.parse(type), beanType, importTypes, false);

    typeReader.process();

    this.requestParams = new BeanRequestParams(type);
    this.injectMethods = typeReader.injectMethods();
    this.injectFields = typeReader.injectFields();
    typeReader.preDestroyPriority();
    this.constructor = typeReader.constructor();
    constructor.params().stream()
        .filter(MethodParam::assisted)
        .map(MethodParam::element)
        .forEach(assistedElements::add);
    injectFields.stream()
        .filter(FieldReader::assisted)
        .map(FieldReader::element)
        .forEach(assistedElements::add);
    injectMethods.stream()
        .map(MethodReader::params)
        .flatMap(List::stream)
        .filter(MethodParam::assisted)
        .map(MethodParam::element)
        .forEach(assistedElements::add);
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement beanType() {
    return beanType;
  }

  AssistBeanReader read() {
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

    return this;
  }

  List<Dependency> dependsOn() {
    List<Dependency> list = new ArrayList<>();
    if (constructor != null) {
      for (MethodReader.MethodParam param : constructor.params()) {
        Dependency dependsOn = param.dependsOn();
        // BeanScope is always injectable with no impact on injection ordering
        if (!Constants.BEANSCOPE.equals(dependsOn.dependsOn())) {
          list.add(dependsOn);
        }
      }
    }
    return list;
  }

  /** Return the short name of the element. */
  private String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  boolean isExtraInjectionRequired() {
    return !injectFields.isEmpty() || !injectMethods.isEmpty();
  }

  void buildRegister(Append writer) {
    writer.indent("    ");
    if (!isExtraInjectionRequired()) {
      writer.append("return bean;");
    }
  }

  private Set<String> importTypes() {
    importTypes.add("io.avaje.inject.AssistInjector");
    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    typeReader.extraImports(importTypes);
    requestParams.addImports(importTypes);
    checkImports();
    if (!suppressGeneratedImport) {
      importTypes.add(Constants.GENERATED);
    }
    if (!suppressBuilderImport) {
      importTypes.add(Constants.BUILDER);
    }

    constructor.addImports(importTypes);
    injectFields.forEach(r -> r.addImports(importTypes));
    injectMethods.forEach(r -> r.addImports(importTypes));
    return importTypes.forImport();
  }

  private void checkImports() {
    suppressBuilderImport = importTypes.containsShortName("Builder");
    suppressGeneratedImport = importTypes.containsShortName("Generated");
  }

  String generatedType() {
    return suppressGeneratedImport ? "@io.avaje.inject.spi.Generated" : "@Generated";
  }

  void writeImports(Append writer) {
    importTypes.add(Constants.COMPONENT);
    if (!injectFields().isEmpty()) {
      importTypes.add(Constants.TYPE);
    }
    for (String importType : importTypes()) {
      if (Util.validImportType(importType)) {
        writer.append("import %s;", Util.sanitizeImports(importType)).eol();
      }
    }
    writer.eol();
  }

  MethodReader constructor() {
    return constructor;
  }

  boolean isWrittenToFile() {
    return writtenToFile;
  }

  void setWrittenToFile() {
    this.writtenToFile = true;
  }

  /**
   * Return true if the bean has a dependency which is a request scoped type. Like Javalin Context,
   * Helidon request and response types.
   *
   * <p>If request scoped then generate a BeanFactory instead.
   */
  boolean isRequestScopedController() {
    return requestParams.isRequestScopedController();
  }

  String suffix() {
    return isRequestScopedController() ? Constants.DOLLAR_FACTORY : Constants.DI;
  }

  /** Add interface for this as a BeanFactory (request scoped). */
  void factoryInterface(Append writer) {
    requestParams.factoryInterface(writer);
  }

  /** Generate the BeanFactory dependencies and create method implementation. */
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
    writer.append("    var bean = new %s(", shortName);
    if (constructor != null) {
      constructor.writeRequestConstructor(writer);
    }
    writer.append(");").eol();
    for (FieldReader field : injectFields) {
      field.writeRequestInject(writer);
    }
    for (final MethodReader method : injectMethods) {
      writer.append("    bean.%s(", method.name());
      method.writeRequestConstructor(writer);
      writer.append(");").eol();
    }
    writer.append("    return bean;").eol();
    writer.append("  }").eol();
  }

  List<FieldReader> injectFields() {
    return typeReader.injectFields();
  }

  List<MethodReader> injectMethods() {
    return typeReader.injectMethods();
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

  String shortName() {
    return Util.shortName(beanQualifiedName());
  }

  String packageName() {
    return beanPackageName();
  }

  private String beanPackageName() {
    if (beanType.getNestingKind().isNested()) {
      return Util.nestedPackageOf(beanQualifiedName());
    } else {
      return Util.packageOf(beanQualifiedName());
    }
  }

  private String beanQualifiedName() {
    return beanType.getQualifiedName().toString();
  }

  boolean needsTryForMethodInjection() {
    for (MethodReader injectMethod : injectMethods) {
      if (injectMethod.methodThrows()) {
        return true;
      }
    }
    return false;
  }

  public List<Element> assistElements() {
    return assistedElements;
  }
}
