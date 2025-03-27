package io.avaje.inject.generator;

final class Constants {

  static final int ORDERING_DEFAULT = 1000;

  static final String DOLLAR_FACTORY = "_Factory";
  static final String DI = "_DI";
  static final String IO_CLOSEABLE = "java.io.Closeable";
  static final String AUTO_CLOSEABLE = "java.lang.AutoCloseable";
  static final String OPTIONAL = "java.util.Optional";
  static final String KOTLIN_METADATA = "kotlin.Metadata";
  static final String TYPE = "java.lang.reflect.Type";

  static final String SINGLETON = "jakarta.inject.Singleton";
  static final String INJECT = "jakarta.inject.Inject";
  static final String SCOPE = "jakarta.inject.Scope";
  static final String QUALIFIER = "jakarta.inject.Qualifier";
  static final String NAMED = "jakarta.inject.Named";

  static final String AT_SINGLETON = "@Singleton";
  static final String AT_PROXY = "@Proxy";
  static final String AT_GENERATED = "@Generated(\"io.avaje.inject.generator\")";
  static final String AT_GENERATED_COMMENT = "(\"io.avaje.inject.generator\")";
  static final String META_INF_SPI = "META-INF/services/io.avaje.inject.spi.InjectExtension";
  static final String META_INF_TESTMODULE = "META-INF/services/io.avaje.inject.test.TestModule";
  static final String META_INF_CUSTOM = "META-INF/services/io.avaje.inject.spi.AvajeModule.Custom";

  static final String BEANSCOPE = "io.avaje.inject.BeanScope";
  static final String INJECTMODULE = "io.avaje.inject.InjectModule";
  static final String TESTSCOPE = "io.avaje.inject.test.TestScope";
  static final String PRIMARY = "io.avaje.inject.Primary";
  static final String SECONDARY = "io.avaje.inject.Secondary";
  static final String PROTOTYPE = "io.avaje.inject.Prototype";
  static final String COMPONENT = "io.avaje.inject.Component";
  static final String FACTORY = "io.avaje.inject.Factory";
  static final String BEAN = "io.avaje.inject.Bean";

  static final String REFLECT_METHOD = "java.lang.reflect.Method";
  static final String ASPECT = "io.avaje.inject.aop.Aspect";
  static final String ASPECT_PROVIDER = "io.avaje.inject.aop.AspectProvider";
  static final String INVOCATION = "io.avaje.inject.aop.Invocation";
  static final String INVOCATION_EXCEPTION = "io.avaje.inject.aop.InvocationException";
  static final String METHOD_INTERCEPTOR = "io.avaje.inject.aop.MethodInterceptor";
  static final String PROXY = "io.avaje.inject.spi.Proxy";

  static final String GENERATED = "io.avaje.inject.spi.Generated";
  static final String BEAN_FACTORY = "io.avaje.inject.spi.BeanFactory";
  static final String BEAN_FACTORY2 = "io.avaje.inject.spi.BeanFactory2";
  static final String BUILDER = "io.avaje.inject.spi.Builder";
  static final String DEPENDENCYMETA = "io.avaje.inject.spi.DependencyMeta";
  static final String MODULE = "io.avaje.inject.spi.AvajeModule";
  static final String GENERICTYPE = "io.avaje.inject.spi.GenericType";

  static final String CONDITIONAL_DEPENDENCY = "con:";
  static final String SOFT_DEPENDENCY = "soft:";
}
