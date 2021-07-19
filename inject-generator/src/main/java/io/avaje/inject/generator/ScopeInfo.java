package io.avaje.inject.generator;

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
  private String name;
  private String contextPackage;

  ScopeInfo(ProcessingContext context) {
    this.context = context;
  }

  void details(String name, Element contextElement) {
    this.name = ScopeUtil.name(name);
    // determine the context package (that we put the DI Factory class into)
    PackageElement pkg = context.getPackageOf(contextElement);
    context.logDebug("using package from element " + pkg);
    this.contextPackage = (pkg == null) ? null : pkg.getQualifiedName().toString();
  }

  String moduleShortName(String factoryPackage) {
    if (name == null || name.isEmpty()) {
      name = ScopeUtil.name(factoryPackage);
    }
    return name + "Module";
  }

  String name() {
    return name;
  }

  String contextPackage() {
    return contextPackage;
  }

  public void provides(List<String> provides) {
    this.provides.addAll(provides);
  }

  void requires(List<String> contextRequires) {
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

  void writeBeanFactory() {
    MetaDataOrdering ordering = new MetaDataOrdering(metaData.values(), context, this);
    int remaining = ordering.processQueue();
    if (remaining > 0) {
      ordering.logWarnings();
    }

    try {
      SimpleFactoryWriter factoryWriter = new SimpleFactoryWriter(ordering, context, this);
      factoryWriter.write();
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
    if (processingOver) {
      writeBeanFactory();
    }
  }

  void buildAtInjectModule(Append writer) {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@InjectModule(");
    if (!provides.isEmpty()) {
      attributeClasses(writer, "provides", provides);
    }
    if (!requires.isEmpty()) {
      attributeClasses(writer, "requires", requires);
    }
    writer.append(")").eol();
  }

  private void attributeClasses(Append writer, String prefix, Set<String> classNames) {
    writer.append(", %s={", prefix);
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
    writer.append(";").eol();
  }
}
