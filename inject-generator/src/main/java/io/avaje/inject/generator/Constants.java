package io.avaje.inject.generator;

class Constants {

  static final String FACTORY = "$Factory";
  static final String DI = "$DI";
  static final String IO_CLOSEABLE = "java.io.Closeable";
  static final String AUTO_CLOSEABLE = "java.lang.AutoCloseable";
  static final String OPTIONAL = "java.util.Optional";
  static final String KOTLIN_METADATA = "kotlin.Metadata";
  static final String TYPE = "java.lang.reflect.Type";

  static final String PROVIDER = "jakarta.inject.Provider";
  static final String SINGLETON = "jakarta.inject.Singleton";
  static final String INJECT = "jakarta.inject.Inject";
  static final String TESTSCOPE = "io.avaje.inject.test.TestScope";

  static final String PATH = "io.avaje.http.api.Path";
  static final String CONTROLLER = "io.avaje.http.api.Controller";

  static final String AT_SINGLETON = "@Singleton";
  static final String AT_PROXY = "@Proxy";
  static final String AT_GENERATED = "@Generated(\"io.avaje.inject.generator\")";
  static final String META_INF_MODULE = "META-INF/services/io.avaje.inject.spi.Module";
  static final String META_INF_TESTMODULE = "META-INF/services/io.avaje.inject.test.TestModule";
  static final String META_INF_CUSTOM = "META-INF/services/io.avaje.inject.spi.Module.Custom";

  static final String BEANSCOPE = "io.avaje.inject.BeanScope";
  static final String INJECTMODULE = "io.avaje.inject.InjectModule";

  static final String REFLECT_METHOD = "java.lang.reflect.Method";
  static final String INVOCATION = "io.avaje.inject.Invocation";
  static final String INVOCATION_EXCEPTION = "io.avaje.inject.InvocationException";
  static final String METHOD_INTERCEPTOR = "io.avaje.inject.MethodInterceptor";
  static final String PROXY = "io.avaje.inject.spi.Proxy";

  static final String GENERATED = "io.avaje.inject.spi.Generated";
  static final String BEAN_FACTORY = "io.avaje.inject.spi.BeanFactory";
  static final String BEAN_FACTORY2 = "io.avaje.inject.spi.BeanFactory2";
  static final String BUILDER = "io.avaje.inject.spi.Builder";
  static final String DEPENDENCYMETA = "io.avaje.inject.spi.DependencyMeta";
  static final String MODULE = "io.avaje.inject.spi.Module";
  static final String GENERICTYPE = "io.avaje.inject.spi.GenericType";

}
