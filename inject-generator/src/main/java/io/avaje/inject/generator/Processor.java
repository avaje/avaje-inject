package io.avaje.inject.generator;

import io.avaje.inject.Factory;
import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;
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
  private CustomScopes customScopes;
  private boolean readModuleInfo;

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
    this.scopeInfo = new ScopeInfo(context, true);
    this.customScopes = new CustomScopes(context);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(InjectModule.class.getCanonicalName());
    annotations.add(Factory.class.getCanonicalName());
    annotations.add(Singleton.class.getCanonicalName());
    annotations.add(Scope.class.getCanonicalName());
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
    Set<? extends Element> scopes = roundEnv.getElementsAnnotatedWith(Scope.class);
    readScopes(scopes);
    readModule(roundEnv);
    readChangedBeans(factoryBeans, true);
    readChangedBeans(beans, false);
    readChangedBeans(controllers, false);
    customScopes.readBeans(roundEnv);

    scopeInfo.write(roundEnv.processingOver());
    customScopes.write(roundEnv.processingOver());
    return false;
  }

  private void readScopes(Set<? extends Element> scopes) {
    for (Element element : scopes) {
      if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
        context.logWarn("detected scope annotation " + element);
        TypeElement type = (TypeElement) element;
        customScopes.addAnnotation(type);
      }
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
        scopeInfo.read((TypeElement) element, factory);
      }
    }
  }

  /**
   * Read the existing meta data from InjectModule (if found) and the factory bean (if exists).
   */
  private void readModule(RoundEnvironment roundEnv) {
    if (readModuleInfo) {
      return;
    }
    String factory = context.loadMetaInfServices();
    if (factory != null) {
      TypeElement moduleType = elementUtils.getTypeElement(factory);
      if (moduleType != null) {
        context.logDebug("Reading module info");
        readModuleDetails(moduleType);
        String builderName = factory.substring(0, factory.length() - 6) + "BeanFactory";
        TypeElement builderType = elementUtils.getTypeElement(builderName);
        if (builderType != null) {
          context.logDebug("Reading module meta data from: " + builderName);
          readFactoryMetaData(builderType);
        }
        readModuleInfo = true;
      }
    }

    Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(InjectModule.class);
    if (!elementsAnnotatedWith.isEmpty()) {
      Iterator<? extends Element> iterator = elementsAnnotatedWith.iterator();
      if (iterator.hasNext()) {
        Element element = iterator.next();
        Scope scope = element.getAnnotation(Scope.class);
        if (scope != null) {
          context.logDebug("Custom scope on " + element);
        } else {
          InjectModule annotation = element.getAnnotation(InjectModule.class);
          if (annotation != null) {
            scopeInfo.details(annotation.name(), element);
          }
        }
      }
    }
  }

  /**
   * Read the existing factory bean. Each of the build methods is annotated with <code>@DependencyMeta</code>
   * which holds the information we need (to regenerate the factory with any changes).
   */
  private void readModuleDetails(TypeElement moduleType) {
    InjectModule module = moduleType.getAnnotation(InjectModule.class);
    scopeInfo.details(module.name(), moduleType);
  }

  private void readFactoryMetaData(TypeElement factoryType) {
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
