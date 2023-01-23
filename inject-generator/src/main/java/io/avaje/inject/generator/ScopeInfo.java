package io.avaje.inject.generator;

import io.avaje.inject.InjectModule;
import io.avaje.inject.spi.DependencyMeta;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.*;

final class ScopeInfo {

  enum Type {
    /**
     * Default scope.
     */
    DEFAULT("Module"),
    /**
     * Custom scope.
     */
    CUSTOM("Module.Custom"),
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
  private final Map<String, MetaData> metaData = new LinkedHashMap<>();
  private final Map<String, String> constructorDependencies = new LinkedHashMap<>();
  private final List<BeanReader> beanReaders = new ArrayList<>();
  private final Set<String> readBeans = new HashSet<>();
  private final ProcessingContext context;
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
  ScopeInfo(ProcessingContext context) {
    this.scopes = null;
    this.context = context;
    this.defaultScope = true;
    this.annotationType = null;
  }

  /**
   * Create for custom scope.
   */
  ScopeInfo(ProcessingContext context, TypeElement type, AllScopes scopes) {
    this.scopes = scopes;
    this.context = context;
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
    ignoreSingleton = ScopeUtil.readIgnoreSingleton(element);
    requires(ScopeUtil.readRequires(element));
    provides(ScopeUtil.readProvides(element));
    for (String require : ScopeUtil.readRequiresPackages(element)) {
      requiresPackages.add(require);
      requirePkg.add(Util.packageOf(require) + ".");
    }
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
      moduleFile = context.createWriter(moduleFullName);
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

  private void provides(List<String> provides) {
    this.provides.addAll(provides);
  }

  private void requires(List<String> contextRequires) {
    this.requires.addAll(contextRequires);
  }

  Set<String> requires() {
    return requires;
  }

  Set<String> pluginProvided() {
    return pluginProvided;
  }

  void writeBeanHelpers() {
    for (BeanReader beanReader : beanReaders) {
      try {
        if (!beanReader.isWrittenToFile()) {
          if (beanReader.isGenerateProxy()) {
            SimpleBeanProxyWriter proxyWriter = new SimpleBeanProxyWriter(beanReader, context);
            proxyWriter.write();
          } else {
            SimpleBeanWriter writer = new SimpleBeanWriter(beanReader, context);
            writer.write();
          }
          beanReader.setWrittenToFile();
        }
      } catch (FilerException e) {
        context.logWarn("FilerException to write $DI class " + beanReader.beanType() + " " + e.getMessage());

      } catch (IOException e) {
        e.printStackTrace();
        context.logError(beanReader.beanType(), "Failed to write $DI class");
      }
    }
  }

  private void initialiseModule() {
    if (!moduleInitialised) {
      try {
        initialiseName(MetaTopPackage.of(metaData.values()));
        context.addModule(moduleFullName);
        moduleInitialised = true;
      } catch (IOException e) {
        if (context.isDuplicateModule(moduleFullName)) {
          String msg = "Attempting to create 2 modules both called " + moduleFullName
            + ". This can occur when a custom scope (named from it's annotation) has a name clash with" +
            " the default module which can be named from the package. Look to resolve this by either " +
            "changing the name of the custom scope annotation, or explicitly naming the default scope " +
            "using @InjectModule(name), or changing the top level package used by the default scope";
          throw new IllegalStateException(msg);
        }
        context.logError("Failed to create module filer " + e.getMessage());
      }
    }
  }

  void writeModule() {
    if (moduleWritten) {
      context.logError("already written module " + name);
      return;
    }
    final Collection<MetaData> meta = metaData.values();
    if (emptyModule) {
      // typically nothing in the default scope, only custom scopes
      if (meta.size() > 0) {
        String msg = meta + " is being ignored by avaje-inject as there is no 'default' module. This is expected when " +
          meta + " is a @Singleton being generated by an annotation processor but there is no 'default' module " +
          "(only custom modules are being used).";
        context.logWarn(msg);
      }
      return;
    }
    MetaDataOrdering ordering = new MetaDataOrdering(meta, context, this);
    int remaining = ordering.processQueue();
    if (remaining > 0) {
      ordering.logWarnings();
    }
    try {
      SimpleModuleWriter factoryWriter = new SimpleModuleWriter(ordering, context, this);
      factoryWriter.write(type());
      moduleWritten = true;
    } catch (FilerException e) {
      context.logWarn("FilerException trying to write factory " + e.getMessage());
    } catch (IOException e) {
      context.logError("Failed to write factory " + e.getMessage());
    }
  }

  /**
   * Return the type of this scope.
   */
  Type type() {
    return annotationType == null ? Type.DEFAULT : Constants.TESTSCOPE.equals(annotationType.getQualifiedName().toString()) ? Type.TEST : Type.CUSTOM;
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
  private void readBeanMeta(TypeElement typeElement, boolean factory) {
    if (typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
      context.logDebug("skipping annotation type " + typeElement);
      return;
    }
    beanReaders.add(new BeanReader(typeElement, context, factory).read());
  }

  void readBuildMethodDependencyMeta(Element element) {
    Name simpleName = element.getSimpleName();
    if (simpleName.toString().startsWith("build_")) {
      // read a build method - DependencyMeta
      DependencyMeta meta = element.getAnnotation(DependencyMeta.class);
      if (meta == null) {
        context.logError("Missing @DependencyMeta on method " + simpleName);
      } else {
        final MetaData metaData = new MetaData(meta);
        this.metaData.put(metaData.key(), metaData);
      }
    }
  }

  void read(TypeElement element, boolean factory) {
    if (readBeans.add(element.toString())) {
      readBeanMeta(element, factory);
    } else {
      context.logDebug("skipping already processed bean " + element);
    }
  }

  void write(boolean processingOver) {
    mergeMetaData();
    writeBeanHelpers();
    initialiseModule();
    if (processingOver && !metaData.isEmpty()) {
      writeModule();
    }
  }

  void buildAtInjectModule(Append writer) {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@InjectModule(");
    boolean leadingComma = false;
    if (!provides.isEmpty()) {
      attributeClasses(false, writer, "provides", provides);
      leadingComma = true;
    }
    if (!requires.isEmpty()) {
      attributeClasses(leadingComma, writer, "requires", requires);
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
      writer.append("customScopeType=\"%s\"", annotationType.getQualifiedName().toString());
    }
    writer.append(")").eol();
  }

  private void attributeClasses(boolean leadingComma, Append writer, String prefix, Set<String> classNames) {
    if (leadingComma) {
      writer.append(", ");
    }
    writer.append("%s={", prefix);
    int c = 0;
    for (String value : classNames) {
      if (c++ > 0) {
        writer.append(",");
      }
      writer.append(value).append(".class");
    }
    writer.append("}");
  }

  void buildProvides(Append writer) {
    if (!provides.isEmpty()) {
      buildProvidesMethod(writer, "provides", provides);
    }
    if (!requires.isEmpty()) {
      buildProvidesMethod(writer, "requires", requires);
    }
    if (!requiresPackages.isEmpty()) {
      buildProvidesMethod(writer, "requiresPackages", requiresPackages);
    }
  }

  private void buildProvidesMethod(Append writer, String fieldName, Set<String> types) {
    writer.append("  @Override").eol();
    writer.append("  public Class<?>[] %s() { return %s; }", fieldName, fieldName).eol();
    writer.append("  private final Class<?>[] %s = new Class<?>[]{", fieldName).eol();
    for (String rawType : types) {
      writer.append("    %s.class,", rawType).eol();
    }
    writer.append("  };").eol().eol();
  }

  void buildAutoProvides(Append writer, Set<String> autoProvides) {
    autoProvides.removeAll(provides);
    if (!autoProvides.isEmpty()) {
      buildProvidesMethod(writer, "autoProvides", autoProvides);
    }
  }

  void buildAutoRequires(Append writer, Set<String> autoRequires) {
    autoRequires.removeAll(requires);
    if (!autoRequires.isEmpty()) {
      buildProvidesMethod(writer, "autoRequires", autoRequires);
    }
  }

  void readModuleMetaData(TypeElement moduleType) {
    InjectModule module = moduleType.getAnnotation(InjectModule.class);
    details(module.name(), moduleType);
    readFactoryMetaData(moduleType);
  }

  private void readFactoryMetaData(TypeElement moduleType) {
    List<? extends Element> elements = moduleType.getEnclosedElements();
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
      if (requiredScope != null) {
        // recursively search parent scope
        if (requiredScope.providesDependencyRecursive(dependency)) {
          // context.logWarn("dependency " + dependency + " provided by other scope " + requiredScope.name);
          return true;
        }
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
      final ScopeInfo otherScope = scopes.get(require);
      if (otherScope == null) {
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
