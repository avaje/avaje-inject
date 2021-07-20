package io.avaje.inject.generator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
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
  }

  static class Data {
    final ScopeInfo scopeInfo;

    Data(TypeElement type, ProcessingContext context) {
      this.scopeInfo = new ScopeInfo(context, false);
      //TODO read name and read InjectModule(provides, requires)
      this.scopeInfo.details(null, type);
    }

    void write(boolean processingOver) {
      scopeInfo.write(processingOver);
    }
  }
}
