package io.avaje.inject.generator;

import io.avaje.inject.InjectModule;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AllScopes {

  private final Map<String, Data> scopeAnnotations = new HashMap<>();
  private final ProcessingContext context;
  private final ScopeInfo defaultScope;

  AllScopes(ProcessingContext context) {
    this.context = context;
    this.defaultScope = new ScopeInfo(context);
  }

  ScopeInfo defaultScope() {
    return defaultScope;
  }

  ScopeInfo addAnnotation(TypeElement type) {
    final Data data = new Data(type, context, this);
    scopeAnnotations.put(type.getQualifiedName().toString(), data);
    return data.scopeInfo;
  }

  boolean providedByDefaultModule(String dependency) {
    return defaultScope.providesDependency(dependency);
  }

  void readBeans(RoundEnvironment roundEnv) {
    for (Data data : scopeAnnotations.values()) {
      for (Element customBean : roundEnv.getElementsAnnotatedWith(data.type)) {
        // context.logWarn("read custom scope bean " + customBean + " for scope " + entry.getKey());
        data.scopeInfo.read((TypeElement) customBean, false);
      }
    }
  }

  void write(boolean processingOver) {
    for (Data value : scopeAnnotations.values()) {
      value.write(processingOver);
    }
    if (processingOver) {
      writeModuleCustomServicesFile();
    }
  }

  private void writeModuleCustomServicesFile() {
    if (scopeAnnotations.isEmpty()) {
      return;
    }
    try {
      FileObject jfo = context.createMetaInfModuleCustom();
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        for (Data value : scopeAnnotations.values()) {
          writer.write(value.moduleFullName());
          writer.write("\n");
        }
        writer.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
      context.logError("Failed to write services file " + e.getMessage());
    }
  }

  void readModules(List<String> customScopeModules) {
    for (String customScopeModule : customScopeModules) {
      final TypeElement module = context.element(customScopeModule);
      if (module != null) {
        final InjectModule injectModule = module.getAnnotation(InjectModule.class);
        if (injectModule != null) {
          final String customScopeType = injectModule.customScopeType();
          final TypeElement scopeType = context.element(customScopeType);
          if (scopeType == null) {
            context.logError(module, "customScopeType [" + customScopeType + "] is invalid? on " + module);
          } else {
            final ScopeInfo scopeInfo = addAnnotation(scopeType);
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

    Data(TypeElement type, ProcessingContext context, AllScopes allScopes) {
      this.type = type;
      this.scopeInfo = new ScopeInfo(context, type, allScopes);
      this.scopeInfo.details(null, type);
    }

    void write(boolean processingOver) {
      scopeInfo.write(processingOver);
    }

    String moduleFullName() {
      return scopeInfo.moduleFullName();
    }
  }
}
