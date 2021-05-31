package io.avaje.inject.generator;

class Constants {

  static final String FACTORY = "$Factory";
  static final String DI = "$DI";
  static final String IO_CLOSEABLE = "java.io.Closeable";
  static final String AUTO_CLOSEABLE = "java.lang.AutoCloseable";
  static final String OPTIONAL = "java.util.Optional";
  static final String KOTLIN_METADATA = "kotlin.Metadata";

  static final String PROVIDER = "jakarta.inject.Provider";
  static final String SINGLETON = "jakarta.inject.Singleton";
  static final String INJECT = "jakarta.inject.Inject";
  static final String REQUEST = "io.avaje.inject.Request";

  static final String PATH = "io.avaje.http.api.Path";
  static final String CONTROLLER = "io.avaje.http.api.Controller";
  static final String REQUESTSCOPEPROVIDER = "io.avaje.inject.RequestScopeProvider";

  static final String AT_SINGLETON = "@Singleton";
  static final String AT_GENERATED = "@Generated(\"io.avaje.inject.generator\")";
  static final String META_INF_FACTORY = "META-INF/services/io.avaje.inject.spi.BeanScopeFactory";

  static final String REQUESTSCOPE = "io.avaje.inject.RequestScope";
  static final String BEANCONTEXT = "io.avaje.inject.BeanScope";
  static final String INJECTMODULE = "io.avaje.inject.InjectModule";

  static final String GENERATED = "io.avaje.inject.spi.Generated";
  static final String BEAN_FACTORY = "io.avaje.inject.spi.BeanFactory";
  static final String BEAN_FACTORY2 = "io.avaje.inject.spi.BeanFactory2";
  static final String BUILDER = "io.avaje.inject.spi.Builder";
  static final String DEPENDENCYMETA = "io.avaje.inject.spi.DependencyMeta";
  static final String BEANSCOPEFACTORY = "io.avaje.inject.spi.BeanScopeFactory";
}
