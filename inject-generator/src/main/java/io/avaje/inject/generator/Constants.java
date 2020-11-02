package io.avaje.inject.generator;

class Constants {

  static final String KOTLIN_METADATA = "kotlin.Metadata";
  static final String GENERATED_9 = "javax.annotation.processing.Generated";

  static final String PROVIDER = "javax.inject.Provider";

  static final String PATH = "io.avaje.http.api.Path";
  static final String CONTROLLER = "io.avaje.http.api.Controller";

  static final String AT_SINGLETON = "@Singleton";
  static final String AT_GENERATED = "@Generated(\"io.avaje.inject.generator\")";
  static final String META_INF_FACTORY = "META-INF/services/io.avaje.inject.spi.BeanContextFactory";

  static final String BEAN_FACTORY = "io.avaje.inject.spi.BeanFactory";
  static final String BEAN_FACTORY2 = "io.avaje.inject.spi.BeanFactory2";
  static final String BEAN_LIFECYCLE = "io.avaje.inject.spi.BeanLifecycle";
  static final String BUILDER = "io.avaje.inject.spi.Builder";
  static final String SINGLETON = "javax.inject.Singleton";
  static final String INJECT = "javax.inject.Inject";

  static final String IMPORT_CONTEXTMODULE = "import io.avaje.inject.ContextModule;";
  static final String IMPORT_DEPENDENCYMETA = "import io.avaje.inject.spi.DependencyMeta;";
  static final String IMPORT_BEANCONTEXT = "import io.avaje.inject.BeanContext;";
  static final String IMPORT_BEANCONTEXTFACTORY = "import io.avaje.inject.spi.BeanContextFactory;";
  static final String IMPORT_BUILDERFACTORY = "import io.avaje.inject.spi.BuilderFactory;";
  static final String IMPORT_BUILDER = "import io.avaje.inject.spi.Builder;";

  static boolean isBeanLifecycle(String type) {
    return BEAN_LIFECYCLE.equals(type);
  }
}
