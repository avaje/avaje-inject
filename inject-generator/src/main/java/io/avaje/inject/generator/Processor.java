package io.avaje.inject.generator;

import io.avaje.inject.Factory;
import io.avaje.inject.InjectModule;
import jakarta.inject.Singleton;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.*;

public class Processor extends AbstractProcessor {

  private ProcessingContext context;
  private Elements elementUtils;
  private ScopeInfo scopeInfo;

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
    this.scopeInfo = new ScopeInfo(context);
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

    readModule(roundEnv);
    readChangedBeans(factoryBeans, true);
    readChangedBeans(beans, false);
    readChangedBeans(controllers, false);

    scopeInfo.write(roundEnv.processingOver());
    return false;
  }

  /**
   * Read the beans that have changed.
   */
  private void readChangedBeans(Set<? extends Element> beans, boolean factory) {
    for (Element element : beans) {
      if (!(element instanceof TypeElement)) {
        context.logError("unexpected type [" + element + "]");
      } else {
        scopeInfo.read((TypeElement) element, factory);
      }
    }
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
          scopeInfo.details(annotation.name(), annotation.provides(), annotation.dependsOn(), element);
          scopeInfo.requires(ScopeUtil.readRequires(element));
        }
      }
    }
  }

  /**
   * Read the existing factory bean. Each of the build methods is annotated with <code>@DependencyMeta</code>
   * which holds the information we need (to regenerate the factory with any changes).
   */
  private void readFactory(TypeElement factoryType) {
    InjectModule module = factoryType.getAnnotation(InjectModule.class);
    scopeInfo.details(module.name(), module.provides(), module.dependsOn(), factoryType);
    scopeInfo.requires(ScopeUtil.readRequires(factoryType));

    List<? extends Element> elements = factoryType.getEnclosedElements();
    if (elements != null) {
      for (Element element : elements) {
        if (ElementKind.METHOD == element.getKind()) {
          scopeInfo.readBuildMethodDependencyMeta(element);
        }
      }
    }
  }
}
