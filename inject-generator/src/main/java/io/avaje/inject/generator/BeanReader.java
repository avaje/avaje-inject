package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import io.avaje.inject.generator.MethodReader.MethodParam;

import java.util.*;
import java.util.stream.Stream;


final class BeanReader {

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

  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final BeanRequestParams requestParams;
  private final TypeReader typeReader;
  private final boolean prototype;
  private final boolean primary;
  private final boolean secondary;
  private final boolean proxy;
  private final BeanAspects aspects;
  private final BeanConditions conditions = new BeanConditions();
  private final boolean importedComponent;
  private final Integer preDestroyPriority;
  private boolean writtenToFile;
  private boolean suppressBuilderImport;
  private boolean suppressGeneratedImport;
  private Set<GenericType> allGenericTypes;
  private final boolean delayed;

  BeanReader(TypeElement beanType, boolean factory, boolean importedComponent) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.prototype = PrototypePrism.isPresent(beanType);
    this.primary = PrimaryPrism.isPresent(beanType);
    this.secondary = !primary && SecondaryPrism.isPresent(beanType);
    this.typeReader = new TypeReader(GenericType.parse(type), beanType, importTypes, factory);

    typeReader.process();

    this.requestParams = new BeanRequestParams(type);
    this.name = typeReader.name();
    this.aspects = typeReader.hasAspects();
    this.injectMethods = typeReader.injectMethods();
    this.injectFields = typeReader.injectFields();
    this.factoryMethods = typeReader.factoryMethods();
    this.postConstructMethod = typeReader.postConstructMethod();
    this.preDestroyMethod = typeReader.preDestroyMethod();
    this.preDestroyPriority = typeReader.preDestroyPriority();
    this.constructor = typeReader.constructor();
    this.importedComponent = importedComponent && (constructor != null && constructor.isPublic());

    if (ProxyPrism.isPresent(beanType)) {
      this.proxy = true;
      conditions.readAll(APContext.asTypeElement(beanType.getSuperclass()));
    } else {
      conditions.readAll(beanType);
      this.proxy = false;
    }
    this.delayed = shouldDelay();
  }

  //delay until next round if types cannot be resolved
  private boolean shouldDelay() {

    var construct =
        Optional.ofNullable(constructor).map(MethodReader::params).stream()
            .flatMap(List::stream)
            .map(p -> p.element().asType());

    var fields = injectFields.stream().map(FieldReader::element).map(Element::asType);

    var constructFields = Stream.concat(construct, fields);

    var methods =
        injectMethods.stream()
            .map(MethodReader::params)
            .flatMap(List::stream)
            .map(p -> p.element().asType());
    return Stream.concat(constructFields, methods).anyMatch(t -> t.getKind() == TypeKind.ERROR);
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement beanType() {
    return beanType;
  }

  BeanAspects aspects() {
    return aspects;
  }

  boolean prototype() {
    return prototype;
  }

  boolean importedComponent() {
    return importedComponent;
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

    conditions.addImports(importTypes);
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
    conditions.requireTypes.stream()
      .map(t -> new Dependency("con:" + t))
      .forEach(list::add);
    conditions.missingTypes.stream()
      .filter(t -> !t.equals(type))
      .map(t -> new Dependency("con:" + t))
      .forEach(list::add);
    return list;
  }

  List<MethodReader> factoryMethods() {
    return factoryMethods;
  }

  List<String> provides() {
    return typeReader.provides();
  }

  String autoProvides() {
    return typeReader.autoProvides();
  }

  String providesAspect() {
    return typeReader.providesAspect();
  }

  Set<GenericType> allGenericTypes() {
    if (allGenericTypes != null) {
      return allGenericTypes;
    }
    allGenericTypes = new LinkedHashSet<>(typeReader.genericTypes());
    for (FieldReader field : injectFields) {
      field.addDependsOnGeneric(allGenericTypes);
    }
    for (MethodReader method : injectMethods) {
      method.addDependsOnGeneric(allGenericTypes);
    }
    if (constructor != null) {
      constructor.addDependsOnGeneric(allGenericTypes);
    }
    for (MethodReader factoryMethod : factoryMethods()) {
      factoryMethod.addDependsOnGeneric(allGenericTypes);
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
  String metaKey() {
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
      type = beanQualifiedName();
    }
    MetaData metaData = new MetaData(type, name);
    metaData.update(this);
    return metaData;
  }

  boolean isExtraInjectionRequired() {
    return !injectFields.isEmpty() || !injectMethods.isEmpty();
  }

  void buildConditional(Append writer) {
    new ConditionalWriter(writer, conditions).write();
  }

  void buildAddFor(Append writer) {
    writer.append("    if (builder.isAddBeanFor(");
    if (name != null && !name.isEmpty()) {
      writer.append("\"%s\", ", name);
    }
    writer.append(typeReader.typesRegister());
    writer.append(")) {").eol();
  }

  void buildRegister(Append writer) {
    if (prototype) {
      return;
    }
    writer.indent("      ");
    if (isExtraInjectionRequired() || hasLifecycleMethods()) {
      writer.append("var $bean = ");
    }
    writer.append("builder.");
    if (primary) {
      writer.append("asPrimary().");
    } else if (secondary) {
      writer.append("asSecondary().");
    }
    writer.append("register(bean);").eol();
  }

  void addLifecycleCallbacks(Append writer, String indent) {
    if (postConstructMethod != null && !prototype) {
      writer.indent(indent).append(" builder.addPostConstruct($bean::%s);", postConstructMethod.getSimpleName()).eol();
    }
    if (preDestroyMethod != null) {
      prototypeNotSupported("@PreDestroy");
      var priority = preDestroyPriority == null || preDestroyPriority == 1000 ? "" : ", " + preDestroyPriority;
      writer.indent(indent).append(" builder.addPreDestroy($bean::%s%s);", preDestroyMethod.getSimpleName(), priority).eol();
    } else if (typeReader.isClosable() && !prototype) {
      writer.indent(indent).append(" builder.addPreDestroy($bean);").eol();
    }
  }

  void prototypePostConstruct(Append writer, String indent) {
    if (postConstructMethod != null) {
      writer.append("%s bean.%s();", indent, postConstructMethod.getSimpleName()).eol();
    }
  }

  private void prototypeNotSupported(String lifecycle) {
    if (prototype) {
      logError(beanType, "@Prototype scoped bean does not support %s lifecycle method", lifecycle);
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
      Set<GenericType> genericTypes = factoryMethod.genericTypes();
      if (!genericTypes.isEmpty()) {
        importTypes.add(Constants.TYPE);
        importTypes.add(Constants.GENERICTYPE);
        genericTypes.forEach(t->t.addImports(importTypes));
      }
    }
    checkImports();
    if (!suppressGeneratedImport){
      importTypes.add(Constants.GENERATED);
    }
    if (!suppressBuilderImport) {
      importTypes.add(Constants.BUILDER);
    }
    return importTypes.forImport();
  }

  private void checkImports() {
    suppressBuilderImport = importTypes.containsShortName("Builder");
    suppressGeneratedImport = importTypes.containsShortName("Generated");
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
   * Return true if the bean has a dependency which is a request scoped type.
   * Like Javalin Context, Helidon request and response types.
   * <p>
   * If request scoped then generate a BeanFactory instead.
   */
  boolean isRequestScopedController() {
    return requestParams.isRequestScopedController();
  }

  String suffix() {
    return isRequestScopedController() ? Constants.DOLLAR_FACTORY : Constants.DI;
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

  boolean hasConditions() {
    return !conditions.isEmpty();
  }

  String shortName() {
    return Util.shortName(beanQualifiedName());
  }

  String packageName() {
    if (importedComponent) {
      return beanPackageName() + ".di";
    } else {
      return beanPackageName();
    }
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

  public boolean isDelayed() {
    return delayed;
  }
}
