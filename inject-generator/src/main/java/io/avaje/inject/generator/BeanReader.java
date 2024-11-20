package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.logError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import io.avaje.inject.generator.MethodReader.MethodParam;


final class BeanReader {

  private final TypeElement beanType;
  private final String shortName;
  private final String type;
  private final String name;

  private final MethodReader constructor;
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final List<MethodReader> factoryMethods;
  private final List<MethodReader> observerMethods;
  private final Element postConstructMethod;
  private final Element preDestroyMethod;

  private final ImportTypeMap importTypes = new ImportTypeMap();
  private final BeanRequestParams requestParams;
  private final TypeReader typeReader;
  private final boolean prototype;
  private final boolean primary;
  private final boolean secondary;
  private final boolean lazy;
  private final boolean proxy;
  private final BeanAspects aspects;
  private final BeanConditions conditions = new BeanConditions();
  private final boolean importedComponent;
  private final Integer preDestroyPriority;
  private boolean writtenToFile;
  private boolean suppressBuilderImport;
  private boolean suppressGeneratedImport;
  private Set<UType> allUTypes;
  private final boolean delayed;

  BeanReader(TypeElement beanType, boolean factory, boolean importedComponent) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.prototype = PrototypePrism.isPresent(beanType);
    this.primary = PrimaryPrism.isPresent(beanType);
    this.secondary = !primary && SecondaryPrism.isPresent(beanType);
    this.lazy = !FactoryPrism.isPresent(beanType) && LazyPrism.isPresent(beanType);
    final var beantypes = BeanTypesPrism.getOptionalOn(beanType);
    beantypes.ifPresent(p -> Util.validateBeanTypes(beanType, p.value()));
    this.typeReader =
      new TypeReader(
        beantypes,
        UType.parse(beanType.asType()),
        beanType,
        importTypes,
        factory);

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
    this.observerMethods = typeReader.observerMethods();
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

  /**
   * delay until next round if types cannot be resolved
   */
  private boolean shouldDelay() {
    var construct = Optional.ofNullable(constructor)
      .map(MethodReader::params).stream()
      .flatMap(List::stream)
      .map(MethodParam::element);

    var fields = injectFields.stream().map(FieldReader::element);
    var constructFields = Stream.concat(construct, fields);
    var methods = injectMethods.stream()
      .map(MethodReader::params)
      .flatMap(List::stream)
      .map(MethodParam::element);

    return Stream.concat(constructFields, methods)
      .map(Element::asType)
      .anyMatch(t -> t.getKind() == TypeKind.ERROR);
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

  boolean registerProvider() {
    return prototype || lazy;
  }

  boolean lazy() {
    return lazy;
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
    for (MethodReader method : observerMethods) {
      method.addImports(importTypes);
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

    observerMethods.stream()
      .flatMap(m -> m.params().stream().skip(1))
      .forEach(param -> {
        Dependency dependsOn = param.dependsOn();
        // BeanScope is always injectable with no impact on injection ordering
        if (!Constants.BEANSCOPE.equals(dependsOn.dependsOn())) {
          list.add(dependsOn);
        }
      });

    conditions.requireTypes.stream()
      .map(t -> new Dependency(Constants.CONDITIONAL_DEPENDENCY + t))
      .forEach(list::add);
    conditions.missingTypes.stream()
      .filter(t -> !t.equals(type))
      .map(t -> new Dependency(Constants.CONDITIONAL_DEPENDENCY + t))
      .forEach(list::add);
    return list;
  }

  List<MethodReader> factoryMethods() {
    return factoryMethods;
  }

  List<String> provides() {
    return Util.addQualifierSuffix(typeReader.provides(), name);
  }

  List<String> autoProvides() {
    return Util.addQualifierSuffix(typeReader.autoProvides(), name);
  }

  String providesAspect() {
    return typeReader.providesAspect();
  }

  Set<UType> allGenericTypes() {
    if (allUTypes != null) {
      return allUTypes;
    }
    allUTypes = new LinkedHashSet<>(typeReader.genericTypes());
    for (FieldReader field : injectFields) {
      field.addDependsOnGeneric(allUTypes);
    }
    for (MethodReader method : injectMethods) {
      method.addDependsOnGeneric(allUTypes);
    }

    for (MethodReader method : observerMethods) {
      var utype = method.observeParam().getFullUType();
      if (utype.isGeneric()) {
        allUTypes.add(utype);
      }
    }

    if (constructor != null) {
      constructor.addDependsOnGeneric(allUTypes);
    }
    for (MethodReader factoryMethod : factoryMethods()) {
      factoryMethod.addDependsOnGeneric(allUTypes);
    }
    return allUTypes;
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
    String metaType;
    if (beanType.getNestingKind().isNested()) {
      metaType = beanType.getEnclosingElement().toString() + "$" + beanType.getSimpleName();
    } else {
      metaType = beanQualifiedName();
    }
    MetaData metaData = new MetaData(metaType, name);
    metaData.update(this);
    return metaData;
  }

  boolean isExtraInjectionRequired() {
    return !injectFields.isEmpty() || !injectMethods.isEmpty();
  }

  void buildConditional(Append writer) {
    new ConditionalWriter(writer, conditions).write();
  }

  void buildBeanAbsent(Append writer) {
    writer.append("    if (builder.isBeanAbsent(");
    if (name != null && !name.isEmpty()) {
      writer.append("\"%s\", ", name);
    }
    writer.append(typeReader.typesRegister());
    writer.append(")) {").eol();
  }

  void buildRegister(Append writer) {
    if (prototype || lazy) {
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

    if (postConstructMethod != null && !registerProvider()) {
      writer.indent(indent).append(" builder.addPostConstruct($bean::%s);", postConstructMethod.getSimpleName()).eol();
    }
    if (preDestroyMethod != null) {
      lifeCycleNotSupported("@PreDestroy");
      var priority = preDestroyPriority == null || preDestroyPriority == 1000 ? "" : ", " + preDestroyPriority;
      writer.indent(indent).append(" builder.addPreDestroy($bean::%s%s);", preDestroyMethod.getSimpleName(), priority).eol();
    } else if (typeReader.isClosable() && !prototype) {
      writer.indent(indent).append(" builder.addPreDestroy($bean);").eol();
    }
  }

  void prototypePostConstruct(Append writer, String indent) {
    if (postConstructMethod != null) {
      var postConstruct = (ExecutableElement) postConstructMethod;
      writer.indent(indent).append(" bean.%s(", postConstructMethod.getSimpleName());
      if (postConstruct.getParameters().isEmpty()) {
        writer.append(");").eol();
      } else {
        writer.append("builder.get(io.avaje.inject.BeanScope.class));").eol();
      }
      writer.eol();
    }
  }

  private void lifeCycleNotSupported(String lifecycle) {
    if (registerProvider()) {
      logError(
        beanType,
        "%s scoped bean does not support the %s lifecycle method",
        prototype ? "@Prototype" : "@Lazy",
        lifecycle);
    }
  }

  private Set<String> importTypes() {
    importTypes.add(type);
    typeReader.extraImports(importTypes);
    requestParams.addImports(importTypes);
    aspects.extraImports(importTypes);

    for (MethodReader factoryMethod : factoryMethods) {
      Set<UType> utypes = factoryMethod.genericTypes();
      if (!utypes.isEmpty()) {
        importTypes.add(Constants.TYPE);
        importTypes.add(Constants.GENERICTYPE);
        utypes.forEach(t -> importTypes.addAll(t.importTypes()));
      }
    }
    checkImports();
    if (!suppressGeneratedImport){
      importTypes.add(Constants.GENERATED);
    }
    if (!suppressBuilderImport && !isGenerateProxy()) {
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

  void writeImports(Append writer, String pkgName) {
    if (!allGenericTypes().isEmpty()) {
      importTypes.add(Constants.TYPE);
      importTypes.add(Constants.GENERICTYPE);
    }
    for (String importType : importTypes()) {
      if (Util.validImportType(importType, pkgName)) {
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

  List<MethodReader> observerMethods() {
    return observerMethods;
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
      return ProcessorUtils.packageOf(beanQualifiedName());
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

  boolean isDelayed() {
    return delayed;
  }
}
