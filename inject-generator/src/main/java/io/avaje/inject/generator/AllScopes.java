package io.avaje.inject.generator;

import static io.avaje.inject.generator.ProcessingContext.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;


final class AllScopes {

  private final Map<String, Data> scopeAnnotations = new HashMap<>();

  private final ScopeInfo defaultScope;

  AllScopes() {
    this.defaultScope = new ScopeInfo();
  }

  ScopeInfo defaultScope() {
    return defaultScope;
  }

  ScopeInfo addScopeAnnotation(TypeElement type) {
    String key = type.getQualifiedName().toString();
    Data existing = scopeAnnotations.get(key);
    if (existing != null) {
      return existing.scopeInfo;
    }
    final Data data = new Data(type, this);
    scopeAnnotations.put(key, data);
    return data.scopeInfo;
  }

  boolean providedByDefaultScope(String dependency) {
    return defaultScope.providesDependencyLocally(dependency);
  }

  void readBeans(RoundEnvironment roundEnv) {
    for (Data data : scopeAnnotations.values()) {
      for (Element customBean : roundEnv.getElementsAnnotatedWith(data.type)) {
        if (customBean instanceof TypeElement) {
          data.scopeInfo.read((TypeElement) customBean, false);
        }
      }
    }
  }

  void write(boolean processingOver) {
    for (Data value : scopeAnnotations.values()) {
      value.write(processingOver);
    }
    // after all scopes have read their meta-data write
    // custom modules (not in last round of processing)
    for (Data value : scopeAnnotations.values()) {
      value.writeCustomModule();
    }
  }

  void readModules(List<String> customScopeModules) {
    for (String customScopeModule : customScopeModules) {
      final TypeElement module = element(customScopeModule);
      if (module != null) {
        InjectModulePrism injectModule = InjectModulePrism.getInstanceOn(module);
        if (injectModule != null) {
          final String customScopeType = injectModule.customScopeType();
          final TypeElement scopeType = element(customScopeType);
          if (scopeType == null) {
            logError(module, "customScopeType [" + customScopeType + "] is invalid? on " + module);
          } else {
            final ScopeInfo scopeInfo = addScopeAnnotation(scopeType);
            scopeInfo.readModuleMetaData(module);
          }
        }
      }
    }
  }

  /**
   * Find the scope by scope annotation type.
   */
  ScopeInfo get(String fullType) {
    final Data data = scopeAnnotations.get(fullType);
    return data == null ? null : data.scopeInfo;
  }

  static class Data {
    final TypeElement type;
    final ScopeInfo scopeInfo;

    Data(TypeElement type, AllScopes allScopes) {
      this.type = type;
      this.scopeInfo = new ScopeInfo(type, allScopes);
      this.scopeInfo.details(null, type);
    }

    void write(boolean processingOver) {
      scopeInfo.write(processingOver);
    }

    void writeCustomModule() {
      scopeInfo.writeCustomModule();
    }
  }
}
