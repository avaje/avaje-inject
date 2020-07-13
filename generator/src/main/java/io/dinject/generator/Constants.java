package io.dinject.generator;

class Constants {

  static final String KOTLIN_METADATA = "kotlin.Metadata";
  static final String GENERATED_9 = "javax.annotation.processing.Generated";
  static final String GENERATED_8 = "javax.annotation.Generated";

  static final String POSTCONSTRUCT = "javax.annotation.PostConstruct";
  static final String PROVIDER = "javax.inject.Provider";

  static final String PATH = "io.dinject.controller.Path";
  static final String CONTROLLER = "io.dinject.controller.Controller";

  static final String AT_GENERATED = "@Generated(\"io.dinject.generator\")";
  static final String META_INF_FACTORY = "META-INF/services/io.dinject.core.BeanContextFactory";

  static final String BEAN_LIFECYCLE = "io.dinject.core.BeanLifecycle";
  static final String BUILDER = "io.dinject.core.Builder";

  static final String IMPORT_BEANCONTEXT = "import io.dinject.BeanContext;";
  static final String IMPORT_CONTEXTMODULE = "import io.dinject.ContextModule;";
  static final String IMPORT_BEANCONTEXTFACTORY = "import io.dinject.core.BeanContextFactory;";
  static final String IMPORT_BUILDERFACTORY = "import io.dinject.core.BuilderFactory;";
  static final String IMPORT_BUILDER = "import io.dinject.core.Builder;";
  static final String IMPORT_DEPENDENCYMETA = "import io.dinject.core.DependencyMeta;";

  static boolean isBeanLifecycle(String type) {
    return BEAN_LIFECYCLE.equals(type);
  }
}
