package io.avaje.inject.generator;

import io.avaje.inject.InjectModule;
import io.avaje.inject.Factory;
import io.avaje.inject.Request;
import io.avaje.inject.spi.DependencyMeta;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private static final String INJECT_MODULE = "io.avaje.inject.InjectModule";

  private ProcessingContext context;

  private Elements elementUtils;

  /**
   * Map to merge the existing meta data with partially compiled code. Keyed by type and qualifier/name.
   */
  private final Map<String, MetaData> metaData = new LinkedHashMap<>();

  private final List<BeanReader> beanReaders = new ArrayList<>();

  private final Set<String> readBeans = new HashSet<>();

  public Processor() {
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.context = new ProcessingContext(processingEnv);
    this.elementUtils = processingEnv.getElementUtils();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {

    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(InjectModule.class.getCanonicalName());
    annotations.add(Factory.class.getCanonicalName());
    annotations.add(Singleton.class.getCanonicalName());
    annotations.add(Constants.CONTROLLER);
    return annotations;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Set<? extends Element> controllers = Collections.emptySet();
    TypeElement typeElement = elementUtils.getTypeElement(Constants.CONTROLLER);
    if (typeElement != null) {
      controllers = roundEnv.getElementsAnnotatedWith(typeElement);
    }

    Set<? extends Element> factoryBeans = roundEnv.getElementsAnnotatedWith(Factory.class);
    Set<? extends Element> beans = roundEnv.getElementsAnnotatedWith(Singleton.class);
    Set<? extends Element> requestBeans = roundEnv.getElementsAnnotatedWith(Request.class);

    readModule(roundEnv);
    readChangedBeans(factoryBeans, true);
    readChangedBeans(beans, false);
    readChangedBeans(controllers, false);
    readChangedBeans(requestBeans, false);

    mergeMetaData();

    writeBeanHelpers();
    if (roundEnv.processingOver()) {
      writeBeanFactory();
    }
    return false;
  }

  private void writeBeanHelpers() {
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

  private void writeBeanFactory() {
    MetaDataOrdering ordering = new MetaDataOrdering(metaData.values(), context);
    int remaining = ordering.processQueue();
    if (remaining > 0) {
      ordering.logWarnings();
    }

    try {
      SimpleFactoryWriter factoryWriter = new SimpleFactoryWriter(ordering, context);
      factoryWriter.write();
    } catch (FilerException e) {
      context.logWarn("FilerException trying to write factory " + e.getMessage());
    } catch (IOException e) {
      context.logError("Failed to write factory " + e.getMessage());
    }
  }

  /**
   * Read the beans that have changed.
   */
  private void readChangedBeans(Set<? extends Element> beans, boolean factory) {
    for (Element element : beans) {
      if (!(element instanceof TypeElement)) {
        context.logError("unexpected type [" + element + "]");
      } else {
        if (readBeans.add(element.toString())) {
          readBeanMeta((TypeElement) element, factory);
        } else {
          context.logDebug("skipping already processed bean " + element);
        }
      }
    }
  }

  /**
   * Merge the changed bean meta data into the existing (factory) metaData.
   */
  private void mergeMetaData() {
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

  /**
   * Read the existing meta data from InjectModule (if found) and the factory bean (if exists).
   */
  private void readModule(RoundEnvironment roundEnv) {
    String factory = context.loadMetaInfServices();
    if (factory != null) {
      TypeElement factoryType = elementUtils.getTypeElement(factory);
      if (factoryType != null) {
        readFactory(factoryType);
      }
    }

    Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(InjectModule.class);
    if (!elementsAnnotatedWith.isEmpty()) {
      Iterator<? extends Element> iterator = elementsAnnotatedWith.iterator();
      if (iterator.hasNext()) {
        Element element = iterator.next();
        InjectModule annotation = element.getAnnotation(InjectModule.class);
        if (annotation != null) {
          context.setContextDetails(annotation.name(), annotation.provides(), annotation.dependsOn(), element);
          context.setContextRequires(readRequires(element));
        }
      }
    }
  }

  /**
   * Read the list of required class names.
   */
  private List<String> readRequires(Element element) {
    List<String> requiresList = new ArrayList<>();
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (INJECT_MODULE.equals(annotationMirror.getAnnotationType().toString())) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
          if (entry.getKey().toString().startsWith("requires")) {
            for (Object requiresType : (List<?>) entry.getValue().getValue()) {
              String fullName = requiresType.toString();
              fullName = fullName.substring(0, fullName.length() - 6);
              requiresList.add(fullName);
            }
          }
        }
      }
    }
    return requiresList;
  }


  /**
   * Read the existing factory bean. Each of the build methods is annotated with <code>@DependencyMeta</code>
   * which holds the information we need (to regenerate the factory with any changes).
   */
  private void readFactory(TypeElement factoryType) {
    InjectModule module = factoryType.getAnnotation(InjectModule.class);
    context.setContextDetails(module.name(), module.provides(), module.dependsOn(), factoryType);
    context.setContextRequires(readRequires(factoryType));

    List<? extends Element> elements = factoryType.getEnclosedElements();
    if (elements != null) {
      for (Element element : elements) {
        if (ElementKind.METHOD == element.getKind()) {
          readBuildMethodDependencyMeta(element);
        }
      }
    }
  }

  private void readBuildMethodDependencyMeta(Element element) {
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
}
