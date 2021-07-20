package io.avaje.inject.generator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CustomScopes {

  private final Map<TypeElement, Data> scopeAnnotations = new HashMap<>();

  private final ProcessingContext context;

  CustomScopes(ProcessingContext context) {
    this.context = context;
  }

  void addAnnotation(TypeElement type) {
    scopeAnnotations.put(type, new Data(type, context));
  }

  void readBeans(RoundEnvironment roundEnv) {
    for (Map.Entry<TypeElement, Data> entry : scopeAnnotations.entrySet()) {
      for (Element customBean : roundEnv.getElementsAnnotatedWith(entry.getKey())) {
        context.logWarn("scope read bean " + customBean);
        entry.getValue().scopeInfo.read((TypeElement) customBean, false);
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
          writer.write(value.factoryFullName());
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
      context.logWarn("load module meta for " + module);
    }
  }

  static class Data {
    final ScopeInfo scopeInfo;

    Data(TypeElement type, ProcessingContext context) {
      this.scopeInfo = new ScopeInfo(context, false);
      this.scopeInfo.details(null, type);
    }

    void write(boolean processingOver) {
      scopeInfo.write(processingOver);
    }

    String factoryFullName() {
      return scopeInfo.factoryFullName();
    }
  }
}
