package io.avaje.inject.generator;

class Constants {

  static final String KOTLIN_METADATA = "kotlin.Metadata";
  static final String GENERATED_9 = "javax.annotation.processing.Generated";

  static final String POSTCONSTRUCT = "javax.annotation.PostConstruct";
  static final String PROVIDER = "javax.inject.Provider";

  static final String PATH = "io.avaje.http.api.Path";
  static final String CONTROLLER = "io.avaje.http.api.Controller";

  static final String AT_SINGLETON = "@Singleton";
  static final String AT_GENERATED = "@Generated(\"io.avaje.inject.generator\")";
  static final String META_INF_FACTORY = "META-INF/services/io.avaje.inject.core.BeanContextFactory";

  static final String BEAN_FACTORY = "io.avaje.inject.core.BeanFactory";
  static final String BEAN_FACTORY2 = "io.avaje.inject.core.BeanFactory2";
  static final String BEAN_LIFECYCLE = "io.avaje.inject.core.BeanLifecycle";
  static final String BUILDER = "io.avaje.inject.core.Builder";
  static final String SINGLETON = "javax.inject.Singleton";
  static final String INJECT = "javax.inject.Inject";

  static final String IMPORT_CONTEXTMODULE = "import io.avaje.inject.ContextModule;";
  static final String IMPORT_DEPENDENCYMETA = "import io.avaje.inject.core.DependencyMeta;";
  static final String IMPORT_BEANCONTEXT = "import io.avaje.inject.BeanContext;";
  static final String IMPORT_BEANCONTEXTFACTORY = "import io.avaje.inject.core.BeanContextFactory;";
  static final String IMPORT_BUILDERFACTORY = "import io.avaje.inject.core.BuilderFactory;";
  static final String IMPORT_BUILDER = "import io.avaje.inject.core.Builder;";

  static boolean isBeanLifecycle(String type) {
    return BEAN_LIFECYCLE.equals(type);
  }
}
