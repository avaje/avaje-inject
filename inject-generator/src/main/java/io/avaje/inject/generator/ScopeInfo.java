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
  private final Set<String> requires = new LinkedHashSet<>();
  private final ProcessingContext context;
  private String name;
  private String[] contextProvides;
  private String[] contextDependsOn;
  private String contextPackage;

  ScopeInfo(ProcessingContext context) {
    this.context = context;
  }

  void details(String name, String[] provides, String[] dependsOn, Element contextElement) {
    this.name = name;
    this.contextProvides = provides;
    this.contextDependsOn = dependsOn;
    // determine the context package (that we put the DI Factory class into)
    PackageElement pkg = context.getPackageOf(contextElement);
    context.logDebug("using package from element " + pkg);
    this.contextPackage = (pkg == null) ? null : pkg.getQualifiedName().toString();
  }

  void deriveName(String factoryPackage) {
    if (name == null) {
      name = factoryPackage;
    }
  }

  String name() {
    return name;
  }

  String contextPackage() {
    return contextPackage;
  }

  void requires(List<String> contextRequires) {
    this.requires.addAll(contextRequires);
  }

  Set<String> requires() {
    return requires;
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

  void buildNewBuilder(Append writer) {
    writer.append("    this.name = \"%s\";", name).eol();
    writer.append("    this.provides = ");
    buildStringArray(writer, contextProvides, true);
    writer.append(";").eol();
    writer.append("    this.dependsOn = ");
    buildStringArray(writer, contextDependsOn, true);
    writer.append(";").eol();
  }

  void buildAtInjectModule(Append writer) {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("@InjectModule(name=\"%s\"", name);
    if (!isEmpty(contextProvides)) {
      writer.append(", provides=");
      buildStringArray(writer, contextProvides, false);
    }
    if (!isEmpty(contextDependsOn)) {
      writer.append(", dependsOn=");
      buildStringArray(writer, contextDependsOn, false);
    }
    if (!requires.isEmpty()) {
      writer.append(", requires={");
      int c = 0;
      for (String value : requires) {
        if (c++ > 0) {
          writer.append(",");
        }
        writer.append(value).append(".class");
      }
      writer.append("}");
    }
    writer.append(")").eol();
  }

  private boolean isEmpty(String[] strings) {
    return strings == null || strings.length == 0;
  }

  private void buildStringArray(Append writer, String[] values, boolean asArray) {
    if (isEmpty(values)) {
      writer.append("null");
    } else {
      if (asArray) {
        writer.append("new String[]");
      }
      writer.append("{");
      int c = 0;
      for (String value : values) {
        if (c++ > 0) {
          writer.append(",");
        }
        writer.append("\"").append(value).append("\"");
      }
      writer.append("}");
    }
  }
}
