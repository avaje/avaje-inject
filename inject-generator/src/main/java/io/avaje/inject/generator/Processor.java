package io.avaje.inject.generator;

import io.avaje.inject.Component;
import io.avaje.inject.Factory;
import io.avaje.inject.InjectModule;
import io.avaje.inject.Prototype;
import io.avaje.inject.spi.Proxy;
import jakarta.inject.Scope;
import jakarta.inject.Singleton;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Processor extends AbstractProcessor {

  private ProcessingContext context;
  private Elements elementUtils;
  private ScopeInfo defaultScope;
  private AllScopes allScopes;
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
    this.allScopes = new AllScopes(context);
    this.defaultScope = allScopes.defaultScope();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(InjectModule.class.getCanonicalName());
    annotations.add(Factory.class.getCanonicalName());
    annotations.add(Singleton.class.getCanonicalName());
    annotations.add(Prototype.class.getCanonicalName());
    annotations.add(Scope.class.getCanonicalName());
    annotations.add(Constants.TESTSCOPE);
    annotations.add(Constants.CONTROLLER);
    return annotations;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    readModule(roundEnv);
    readScopes(roundEnv.getElementsAnnotatedWith(Scope.class));
    readChangedBeans(roundEnv.getElementsAnnotatedWith(Factory.class), true);
    if (defaultScope.includeSingleton()) {
      readChangedBeans(roundEnv.getElementsAnnotatedWith(Singleton.class), false);
    }
    readChangedBeans(roundEnv.getElementsAnnotatedWith(Component.class), false);
    readChangedBeans(roundEnv.getElementsAnnotatedWith(Prototype.class), false);
    TypeElement typeElement = elementUtils.getTypeElement(Constants.CONTROLLER);
    if (typeElement != null) {
      readChangedBeans(roundEnv.getElementsAnnotatedWith(typeElement), false);
    }
    readChangedBeans(roundEnv.getElementsAnnotatedWith(Proxy.class), false);
    allScopes.readBeans(roundEnv);
    defaultScope.write(roundEnv.processingOver());
    allScopes.write(roundEnv.processingOver());
    return false;
  }

  private void readScopes(Set<? extends Element> scopes) {
    for (Element element : scopes) {
      if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
        if (element instanceof TypeElement) {
          TypeElement type = (TypeElement) element;
          allScopes.addScopeAnnotation(type);
        }
      }
    }
    addTestScope();
  }

  /**
   * Add built-in test scope for <code>@TestScope</code> if available.
   */
  private void addTestScope() {
    TypeElement testScopeType = elementUtils.getTypeElement(Constants.TESTSCOPE);
    if (testScopeType != null) {
      allScopes.addScopeAnnotation(testScopeType);
    }
  }

  /**
   * Read the beans that have changed.
   */
  private void readChangedBeans(Set<? extends Element> beans, boolean factory) {
    for (Element element : beans) {
      // ignore methods (e.g. factory methods with @Prototype on them)
      if (element instanceof TypeElement) {
        TypeElement typeElement = (TypeElement) element;
        final ScopeInfo scope = findScope(typeElement);
        if (!factory) {
          // will be found via custom scope so effectively ignore additional @Singleton
          if (scope == null) {
            defaultScope.read(typeElement, false);
          }
        } else {
          if (scope != null) {
            // context.logWarn("Adding factory to custom scope "+element+" scope: "+scope);
            scope.read(typeElement, true);
          } else {
            defaultScope.read(typeElement, true);
          }
        }
      }
    }
  }

  /**
   * Find the scope if the Factory has a scope annotation.
   */
  private ScopeInfo findScope(Element element) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final ScopeInfo scopeInfo = allScopes.get(annotationMirror.getAnnotationType().toString());
      if (scopeInfo != null) {
        return scopeInfo;
      }
    }
    return null;
  }

  /**
   * Read the existing meta data from InjectModule (if found) and the factory bean (if exists).
   */
  private void readModule(RoundEnvironment roundEnv) {
    if (readModuleInfo) {
      // only read the module meta data once
      return;
    }
    readModuleInfo = true;
    String factory = context.loadMetaInfServices();
    if (factory != null) {
      TypeElement moduleType = elementUtils.getTypeElement(factory);
      if (moduleType != null) {
        defaultScope.readModuleMetaData(moduleType);
      }
    }
    allScopes.readModules(context.loadMetaInfCustom());
    readInjectModule(roundEnv);
  }

  /**
   * Read InjectModule for things like package-info etc (not for custom scopes)
   */
  private void readInjectModule(RoundEnvironment roundEnv) {
    // read other that are annotated with InjectModule
    for (Element element : roundEnv.getElementsAnnotatedWith(InjectModule.class)) {
      Scope scope = element.getAnnotation(Scope.class);
      if (scope == null) {
        // it it not a custom scope annotation
        InjectModule annotation = element.getAnnotation(InjectModule.class);
        if (annotation != null) {
          defaultScope.details(annotation.name(), element);
        }
      }
    }
  }

}
