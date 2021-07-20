package io.avaje.inject.generator;

import io.avaje.inject.InjectModule;
import io.avaje.inject.spi.DependencyMeta;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.*;
import java.io.IOException;
import java.util.*;

class ScopeInfo {

  /**
   * Map to merge the existing meta data with partially compiled code. Keyed by type and qualifier/name.
   */
  private final Map<String, MetaData> metaData = new LinkedHashMap<>();
  private final List<BeanReader> beanReaders = new ArrayList<>();
  private final Set<String> readBeans = new HashSet<>();
  private final ProcessingContext context;
  private final Set<String> requires = new LinkedHashSet<>();
  private final Set<String> provides = new LinkedHashSet<>();
  private final boolean mainScope;
  private final TypeElement annotationType;
  private final AllScopes scopes;
  private boolean moduleWritten;
  private boolean builderWritten;
  private String name;
  private String topPackage;
  private String factoryFullName;
  private String factoryShortName;

  /**
   * Create for the main/global module scope.
   */
  ScopeInfo(ProcessingContext context) {
    this.scopes = null;
    this.context = context;
    this.mainScope = true;
    this.annotationType = null;
  }

  /**
   * Create for custom scope.
   */
  ScopeInfo(ProcessingContext context, TypeElement type, AllScopes scopes) {
    this.scopes = scopes;
    this.context = context;
    this.mainScope = false;
    this.annotationType = type;
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
    requires(ScopeUtil.readRequires(element));
    provides(ScopeUtil.readProvides(element));
  }

  private String initName(String factoryPackage) {
    if (name == null || name.isEmpty()) {
      name = ScopeUtil.name(factoryPackage);
    }
    return name;
  }

  void moduleShortName(String factoryPackage) {
    factoryShortName = initName(factoryPackage) + "Module";
    factoryFullName = factoryPackage + "." + factoryShortName;
  }

  String factoryShortName(String factoryPackage) {
    return initName(factoryPackage) + "BeanFactory";
  }

  String factoryFullName() {
    return factoryFullName;
  }

  String factoryShortName() {
    return factoryShortName;
  }

  boolean isMainScope() {
    return mainScope;
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

  Set<String> provides() {
    return provides;
  }

  void writeBeanHelpers() {
    for (BeanReader beanReader : beanReaders) {
      try {
        if (!beanReader.isWrittenToFile()) {
          SimpleBeanWriter writer = new SimpleBeanWriter(beanReader, context);
          writer.write();
          beanReader.setWrittenToFile();
        }
      } catch (FilerException e) {
        context.logWarn("FilerException to write $DI class " + beanReader.getBeanType() + " " + e.getMessage());

      } catch (IOException e) {
        e.printStackTrace();
        context.logError(beanReader.getBeanType(), "Failed to write $DI class");
      }
    }
  }

  private void writeModule() {
    if (!moduleWritten) {
      try {
        this.topPackage = MetaTopPackage.of(metaData.values());
        SimpleModuleWriter factoryWriter = new SimpleModuleWriter(topPackage, context, this);
        factoryWriter.write(mainScope);
        moduleWritten = true;
      } catch (FilerException e) {
        context.logWarn("FilerException trying to write factory " + e.getMessage());
      } catch (IOException e) {
        context.logError("Failed to write factory " + e.getMessage());
      }
    }
  }

  void writeBeanFactory() {
    if (builderWritten) {
      context.logError("already written builder " + name);
      return;
    }
    MetaDataOrdering ordering = new MetaDataOrdering(topPackage, metaData.values(), context, this);
    int remaining = ordering.processQueue();
    if (remaining > 0) {
      ordering.logWarnings();
    }
    try {
      SimpleFactoryWriter factoryWriter = new SimpleFactoryWriter(ordering, context, this);
      factoryWriter.write();
      builderWritten = true;
    } catch (FilerException e) {
      context.logWarn("FilerException trying to write factory " + e.getMessage());
    } catch (IOException e) {
      context.logError("Failed to write factory " + e.getMessage());
    }
  }

  /**
   * Merge the changed bean meta data into the existing (factory) metaData.
   */
  void mergeMetaData() {
    for (BeanReader beanReader : beanReaders) {
      if (!beanReader.isRequestScopedController()) {
        MetaData metaData = this.metaData.get(beanReader.getMetaKey());
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
    metaData.put(meta.getKey(), meta);
    for (MetaData methodMeta : beanReader.createFactoryMethodMeta()) {
      metaData.put(methodMeta.getKey(), methodMeta);
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
        this.metaData.put(metaData.getKey(), metaData);
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
    writeModule();
    if (processingOver) {
      writeBeanFactory();
    }
  }

  void buildAtInjectModule(Append writer) {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@InjectModule(");
    boolean leadingComma = false;
    if (!provides.isEmpty()) {
      attributeClasses(leadingComma, writer, "provides", provides);
      leadingComma = true;
    }
    if (!requires.isEmpty()) {
      attributeClasses(leadingComma, writer, "requires", requires);
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

  private void buildClassArray(Append writer, Set<String> values) {
    writer.append("new Class<?>[]");
    writer.append("{");
    if (!values.isEmpty()) {
      int c = 0;
      for (String value : values) {
        if (c++ > 0) {
          writer.append(",");
        }
        writer.append(value).append(".class");
      }
    }
    writer.append("}");
  }

  void buildFields(Append writer) {
    writer.append("  private final Class<?>[] provides = ");
    buildClassArray(writer, provides);
    writer.append(";").eol();
    writer.append("  private final Class<?>[] requires = ");
    buildClassArray(writer, requires);
    writer.append(";").eol().eol();
  }

  void readModuleMetaData(String factory, TypeElement moduleType) {
    context.logDebug("Reading module info for " + moduleType);
    InjectModule module = moduleType.getAnnotation(InjectModule.class);
    details(module.name(), moduleType);

    String builderName = factory.substring(0, factory.length() - 6) + "BeanFactory";
    TypeElement builderType = context.element(builderName);
    if (builderType != null) {
      // context.logDebug("Reading module meta data from: " + builderName);
      readFactoryMetaData(builderType);
    }
  }

  private void readFactoryMetaData(TypeElement factoryType) {
    List<? extends Element> elements = factoryType.getEnclosedElements();
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
  boolean providedByOtherModule(String dependency) {
    if (mainScope) {
      return false;
    }
    return scopes.providedByDefaultModule(dependency);
  }

  /**
   * Return true if this module provides the dependency.
   */
  boolean providesDependency(String dependency) {
    for (MetaData meta : metaData.values()) {
      if (dependency.equals(meta.getType())) {
        return true;
      }
      final List<String> provides = meta.getProvides();
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
}
