package io.avaje.inject.generator;

import static io.avaje.inject.generator.APContext.createSourceFile;
import static io.avaje.inject.generator.APContext.logError;
import static io.avaje.inject.generator.APContext.logNote;
import static io.avaje.inject.generator.APContext.logWarn;
import static io.avaje.inject.generator.ProcessingContext.addModule;
import static io.avaje.inject.generator.ProcessingContext.isDuplicateModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;


final class ScopeInfo {

  enum Type {
    /**
     * Default scope.
     */
    DEFAULT("AvajeModule"),
    /**
     * Custom scope.
     */
    CUSTOM("AvajeModule.Custom"),
    /**
     * Built-in Test scope.
     */
    TEST("io.avaje.inject.test.TestModule");

    final String type;
    Type(String type) {
      this.type = type;
    }
    String type() {
      return type;
    }
  }

  /**
   * Map to merge the existing meta data with partially compiled code. Keyed by type and qualifier/name.
   */
  private final Map<String, MetaData> metaData = new HashMap<>();
  private final Map<String, String> constructorDependencies = new LinkedHashMap<>();
  private final List<BeanReader> beanReaders = new ArrayList<>();
  private final Set<String> readBeans = new HashSet<>();
  private final Set<String> pluginProvided = new HashSet<>();
  private final Set<String> requires = new LinkedHashSet<>();
  private final Set<String> provides = new LinkedHashSet<>();
  private final Set<String> requiresPackages = new LinkedHashSet<>();
  private final List<String> requirePkg = new ArrayList<>();
  private final boolean defaultScope;
  private final TypeElement annotationType;
  private final AllScopes scopes;
  private boolean moduleInitialised;
  private boolean moduleWritten;
  private String name;
  private String modulePackage;
  private String moduleFullName;
  private String moduleShortName;
  private JavaFileObject moduleFile;
  private boolean emptyModule;
  private boolean ignoreSingleton;

  /**
   * Create for the main/global module scope.
   */
  ScopeInfo() {
    this.scopes = null;
    this.defaultScope = true;
    this.annotationType = null;
  }

  /**
   * Create for custom scope.
   */
  ScopeInfo(TypeElement type, AllScopes scopes) {
    this.scopes = scopes;
    this.defaultScope = false;
    this.annotationType = type;
  }

  @Override
  public String toString() {
    return "ScopeInfo{" +
      "name=" + name +
      ", metaData=" + metaData +
      '}';
  }

  void pluginProvided(String pluginProvides) {
    pluginProvided.add(pluginProvides);
  }

  boolean includeSingleton() {
    return !ignoreSingleton;
  }

  void details(String name, Element contextElement) {
    if (name == null || name.isEmpty()) {
      final String simpleName = contextElement.getSimpleName().toString();
      this.name = ScopeUtil.name(simpleName);
    } else {
      this.name = ScopeUtil.name(name);
    }
    read(contextElement);
  }

  private void read(Element element) {
    final var injectModule = InjectModulePrism.getInstanceOn(element);
    if (injectModule == null) {
      return;
    }
    ignoreSingleton = injectModule.ignoreSingleton();
    injectModule.requires().stream().map(Object::toString).forEach(requires::add);
    injectModule.provides().stream().map(Object::toString).forEach(provides::add);
    requires.addAll(injectModule.requiresString());
    provides.addAll(injectModule.providesString());
    injectModule.requiresPackages().stream()
        .map(Object::toString)
        .forEach(
            require -> {
              requiresPackages.add(require);
              requirePkg.add(ProcessorUtils.packageOf(require) + ".");
            });
  }

  private String initName(String topPackage) {
    if (name == null || name.isEmpty()) {
      name = ScopeUtil.initName(topPackage);
    }
    return name;
  }

  void initialiseName(String topPackage) throws IOException {
    emptyModule = topPackage == null;
    if (!emptyModule) {
      modulePackage = topPackage;
      final String name = initName(modulePackage);
      moduleShortName = name + "Module";
      moduleFullName = modulePackage + "." + moduleShortName;
      moduleFile = createSourceFile(moduleFullName);
    }
  }

  JavaFileObject moduleFile() {
    return moduleFile;
  }

  String modulePackage() {
    return modulePackage;
  }

  String moduleFullName() {
    return moduleFullName;
  }

  String moduleShortName() {
    return moduleShortName;
  }

  String name() {
    return name;
  }

  String scopeAnnotationFQN(){
    return annotationType.getQualifiedName().toString();
  }

  Set<String> requires() {
    return requires;
  }

  Set<String> provides() {
    return provides;
  }

  Set<String> pluginProvided() {
    return pluginProvided;
  }

  List<ScopeInfo> dependentScopes() {
    final List<ScopeInfo> depScopes = new ArrayList<>();
    requires().stream()
      .map(APContext::typeElement)
      .filter(e -> e.getKind() == ElementKind.ANNOTATION_TYPE)
      .filter(ScopePrism::isPresent)
      .map(TypeElement::getQualifiedName)
      .map(Object::toString)
      .map(scopes::get)
      .filter(Objects::nonNull)
      .flatMap(scope -> scope.dependentScopes().stream())
      .forEach(depScopes::add);

    depScopes.add(this);
    return List.copyOf(depScopes);
  }

  void writeBeanHelpers() {
    for (BeanReader beanReader : beanReaders) {
      try {
        if (!beanReader.isWrittenToFile()) {
          if (beanReader.isGenerateProxy()) {
            SimpleBeanProxyWriter proxyWriter = new SimpleBeanProxyWriter(beanReader);
            proxyWriter.write();
          } else {
            SimpleBeanWriter writer = new SimpleBeanWriter(beanReader);
            writer.write();
          }
          beanReader.setWrittenToFile();
        }
      } catch (FilerException e) {
        logWarn("FilerException to write $DI class %s %s", beanReader.beanType(), e.getMessage());
      } catch (IOException e) {
        logError(beanReader.beanType(), "Failed to write $DI class %s", e);
      }
    }
  }

  private void initialiseModule() {
    if (!moduleInitialised) {
      try {
        initialiseName(MetaTopPackage.of(metaData.values()));
        addModule(moduleFullName);
        moduleInitialised = true;
      } catch (IOException e) {
        if (isDuplicateModule(moduleFullName)) {
          String msg = "Attempting to create 2 modules both called " + moduleFullName
            + ". This can occur when a custom scope (named from it's annotation) has a name clash with" +
            " the default module which can be named from the package. Look to resolve this by either " +
            "changing the name of the custom scope annotation, or explicitly naming the default scope " +
            "using @InjectModule(name), or changing the top level package used by the default scope";
          throw new IllegalStateException(msg);
        }
        logError("Failed to create module filer %s", e.getMessage());
      }
    }
  }

  void writeModule() {
    if (moduleWritten) {
      return;
    }
    final Collection<MetaData> meta = metaData.values().stream().sorted().collect(Collectors.toList());
    if (emptyModule) {
      // typically nothing in the default scope, only custom scopes
      if (!meta.isEmpty()) {
        String msg = meta + " is being ignored by avaje-inject as there is no 'default' module. This is expected when " +
          meta + " is a @Singleton being generated by an annotation processor but there is no 'default' module " +
          "(only custom modules are being used).";
        logWarn(msg);
      }
      return;
    }
    final MetaDataOrdering ordering = new MetaDataOrdering(meta, this);
    final int remaining = ordering.processQueue();

    if (remaining > 0) {
      ordering.logWarnings();
    }
    try {
      SimpleModuleWriter factoryWriter = new SimpleModuleWriter(ordering, this);
      factoryWriter.write();
      moduleWritten = true;
    } catch (FilerException e) {
      logWarn("FilerException trying to write factory %s", e.getMessage());
    } catch (IOException e) {
      logError("Failed to write factory %s", e.getMessage());
    }
  }

  /**
   * Return the type of this scope.
   */
  Type type() {
    return annotationType == null ? Type.DEFAULT : Constants.TESTSCOPE.equals(annotationType.getQualifiedName().toString()) ? Type.TEST : Type.CUSTOM;
  }

  UType scopeAnnotation() {
    return UType.parse(annotationType.asType());
  }

  /**
   * Merge the changed bean meta data into the existing (factory) metaData.
   */
  void mergeMetaData() {
    for (BeanReader beanReader : beanReaders) {
      if (!beanReader.isRequestScopedController()) {
        MetaData metaData = this.metaData.get(beanReader.metaKey());
        if (metaData == null) {
          addMeta(beanReader);
        } else {
          updateMeta(metaData, beanReader);
        }
      }
    }
  }

  /**
   * Add a new previously unknown bean.
   */
  private void addMeta(BeanReader beanReader) {
    MetaData meta = beanReader.createMeta();
    metaData.put(meta.key(), meta);
    for (MetaData methodMeta : beanReader.createFactoryMethodMeta()) {
      metaData.put(methodMeta.key(), methodMeta);
    }
  }

  /**
   * Update the meta data on a previously known bean.
   */
  private void updateMeta(MetaData metaData, BeanReader beanReader) {
    metaData.update(beanReader);
  }

  /**
   * Read the dependency injection meta data for the given bean.
   */
  private void readBeanMeta(TypeElement typeElement, boolean factory, boolean importedComponent) {
    if (typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
      logNote("skipping annotation type %s", typeElement);
      return;
    }
    var reader = new BeanReader(typeElement, factory, importedComponent).read();
    if (reader.isDelayed() && ProcessingContext.delayUntilNextRound(typeElement)) {
      readBeans.remove(typeElement.toString());
    } else {
      beanReaders.add(reader);
    }
  }

  void readBuildMethodDependencyMeta(Element element) {
    Name simpleName = element.getSimpleName();
    if (simpleName.toString().startsWith("build_")) {
      // read a build method - DependencyMeta
      DependencyMetaPrism meta = DependencyMetaPrism.getInstanceOn(element);
      if (meta == null) {
        logError("Missing @DependencyMeta on method %s", simpleName);
      } else {
        final MetaData metaData = new MetaData(meta);
        this.metaData.put(metaData.key(), metaData);
      }
    }
  }

  void read(TypeElement element, boolean factory, boolean importedComponent) {
    if (readBeans.add(element.toString())) {
      readBeanMeta(element, factory, importedComponent);
    } else {
      logNote("skipping already processed bean %s", element);
    }
  }

  /**
   * Write Custom modules during processing (not last round) so that they are
   * visible to code in src/main. This means that Custom scopes do NOT support
   * other annotation processors generating beans that use those Custom scopes.
   */
  void writeCustomModule() {
    if (type() == Type.CUSTOM && !metaData.isEmpty()) {
      writeModule();
    }
  }

  void write(boolean processingOver) {
    mergeMetaData();
    writeBeanHelpers();
    initialiseModule();
    if (processingOver && !metaData.isEmpty()) {
      ProcessingContext.registerExternalProvidedTypes(this);
      writeModule();
    }
  }

  void buildAtInjectModule(Append writer) {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@InjectModule(");
    boolean leadingComma = false;
    Set<String> regularProvides = new LinkedHashSet<>();
    Set<String> genericProvides = new LinkedHashSet<>();

    for (var type : provides) {
      if (type.contains("<")) {
        genericProvides.add(type);
      } else {
        regularProvides.add(type);
      }
    }

    if (!regularProvides.isEmpty()) {
      attributeClasses(false, writer, "provides", regularProvides);
      leadingComma = true;
    }
    if (!genericProvides.isEmpty()) {
      attributeString(false, writer, "providesString", genericProvides);
      leadingComma = true;
    }

    Set<String> regularRequires = new LinkedHashSet<>();
    Set<String> genericRequires = new LinkedHashSet<>();

    for (var type : requires) {
      if (type.contains("<")) {
        genericRequires.add(type);
      } else {
        regularRequires.add(type);
      }
    }
    if (!regularRequires.isEmpty()) {
      attributeClasses(leadingComma, writer, "requires", regularRequires);
      leadingComma = true;
    }
    if (!genericRequires.isEmpty()) {
      attributeString(leadingComma, writer, "requiresString", genericRequires);
      leadingComma = true;
    }
    if (!requiresPackages.isEmpty()) {
      attributeClasses(leadingComma, writer, "requiresPackages", requiresPackages);
      leadingComma = true;
    }
    if (annotationType != null) {
      if (leadingComma) {
        writer.append(", ");
      }
      writer.append("customScopeType = \"%s\"", annotationType.getQualifiedName().toString());
    }
    writer.append(")").eol();
  }

  private void attributeString(boolean leadingComma, Append writer, String prefix, Set<String> classNames) {
    if (leadingComma) {
      writer.append(", ");
    }
    writer.append("%s = {", prefix);
    int c = 0;
    for (final String value : classNames) {
      if (c++ > 0) {
        writer.append(",");
      }
      writer.append("\"%s\"", value);
    }
    writer.append("}");
  }

  private void attributeClasses(boolean leadingComma, Append writer, String prefix, Collection<String> classNames) {
    if (leadingComma) {
      writer.append(", ");
    }
    writer.append("%s = {", prefix);
    int c = 0;
    for (final String value : classNames) {
      if (c++ > 0) {
        writer.append(",");
      }
      writer.append(value).append(".class");
    }
    writer.append("}");
  }

  void buildProvides(Append writer, Set<String> provides, Set<String> requires) {
    if (!provides.isEmpty()) {
      buildProvidesMethod(writer, "providesBeans", provides);
    }
    if (!requires.isEmpty()) {
      buildProvidesMethod(writer, "requiresBeans", requires);
    }
    if (!requiresPackages.isEmpty()) {
      buildProvidesMethod(writer, "requiresPackagesFromType", requiresPackages);
    }
  }

  private void buildProvidesMethod(Append writer, String fieldName, Set<String> types) {
    writer.append("  @Override").eol();
    writer.append("  public String[] %s() {", fieldName).eol();
    writer.append("    return new String[] {").eol();
    for (final String rawType : types) {

      if (rawType.contains(":")) {
        continue;
      }
      writer.append("      \"%s\",", rawType).eol();
    }
    writer.append("    };").eol();
    writer.append("  }").eol().eol();
  }

  void readModuleMetaData(TypeElement moduleType) {
    final InjectModulePrism module = InjectModulePrism.getInstanceOn(moduleType);
    final String name = module == null ? null : module.name();
    details(name, moduleType);
    readFactoryMetaData(moduleType);
  }

  private void readFactoryMetaData(TypeElement moduleType) {
    final List<? extends Element> elements = moduleType.getEnclosedElements();
    if (elements != null) {
      for (Element element : elements) {
        if (ElementKind.METHOD == element.getKind()) {
          readBuildMethodDependencyMeta(element);
        }
      }
    }
  }

  /**
   * Return true if the scope is a custom scope and the dependency is provided
   * by the "default" module. We could/should move to be tighter here at some point.
   */
  boolean providedByOtherScope(String dependency) {
    if (defaultScope) {
      return false;
    }
    if (scopes.providedByDefaultScope(dependency)) {
      return true;
    }
    return providesDependencyRecursive(dependency);
  }

  /**
   * Recursively search including 'parent' scopes.
   */
  private boolean providesDependencyRecursive(String dependency) {
    if (providesDependencyLocally(dependency)) {
      return true;
    }
    // look for required scopes ...
    for (String require : requires) {
      final ScopeInfo requiredScope = scopes.get(require);
      // recursively search parent scope
      if (requiredScope != null && requiredScope.providesDependencyRecursive(dependency)) {
        // logWarn("dependency " + dependency + " provided by other scope " + requiredScope.name);
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if this module provides the dependency (non-recursive, local only).
   */
  boolean providesDependencyLocally(String dependency) {
    if (requires.contains(dependency) || pluginProvided.contains(dependency)) {
      return true;
    }
    for (MetaData meta : metaData.values()) {
      if (dependency.equals(meta.type())) {
        return true;
      }
      final List<String> provides = meta.provides();
      if (provides != null && !provides.isEmpty()) {
        for (String provide : provides) {
          if (dependency.equals(provide)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  boolean providedByPackage(String dependency) {
    for (String pkg : requirePkg) {
      if (dependency.startsWith(pkg)) {
        return true;
      }
    }
    return false;
  }

  boolean providedByOther(Dependency dependency) {
    return dependency.isSoftDependency()
      || providedByPackage(dependency.name())
      || providedByOtherScope(dependency.name());
  }

  Set<String> initModuleDependencies(Set<String> importTypes) {
    if (defaultScope || requires.isEmpty()) {
      return importTypes;
    }
    for (String require : requires) {
      if (!ScopePrism.isPresent(APContext.typeElement(require))) {
        importTypes.add(require);
        final String type = Util.shortName(require);
        final String var = Util.initLower(type);
        constructorDependencies.put(type, var);
      }
    }
    return importTypes;
  }

  boolean addModuleConstructor() {
    return !constructorDependencies.isEmpty();
  }

  Map<String, String> constructorDependencies() {
    return constructorDependencies;
  }

  boolean addWithBeans() {
    return !constructorDependencies.isEmpty();
  }
}
