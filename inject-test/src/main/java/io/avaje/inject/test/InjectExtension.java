package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Junit 5 extension for avaje inject.
 * <p>
 * Supports injection for fields annotated with <code>@Mock, @Spy, @Captor, @Inject</code>.
 */
public class InjectExtension implements BeforeEachCallback, AfterEachCallback {//}, ParameterResolver {

  private final static Namespace INJECT_NS = Namespace.create("io.avaje.inject");

  private final static String BEAN_SCOPE = "BEAN_SCOPE";

  /**
   * Callback that is invoked <em>before</em> each test is invoked.
   */
  @Override
  public void beforeEach(final ExtensionContext context) {
    final List<MetaReader> readers = createMetaReaders(context);

    final BeanScopeBuilder builder = BeanScope.newBuilder();
    for (MetaReader reader : readers) {
      reader.build(builder);
    }

    final BeanScope beanScope = builder.build();
    for (MetaReader reader : readers) {
      reader.setFromScope(beanScope);
    }
    context.getStore(INJECT_NS).put(BEAN_SCOPE, beanScope);
  }

  /**
   * Return the list of MetaReaders - 1 per test instance.
   */
  private List<MetaReader> createMetaReaders(ExtensionContext context) {
    final List<Object> testInstances = context.getRequiredTestInstances().getAllInstances();
    final List<MetaReader> readers = new ArrayList<>(testInstances.size());
    for (Object testInstance : testInstances) {
      readers.add(new MetaReader(testInstance));
    }
    return readers;
  }

  /**
   * Callback that is invoked <em>after</em> each test has been invoked.
   */
  @Override
  public void afterEach(ExtensionContext context) {
    final BeanScope beanScope = (BeanScope) context.getStore(INJECT_NS).remove(BEAN_SCOPE);
    if (beanScope != null) {
      beanScope.close();
    }
  }

//  @Override
//  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
//    return parameterContext.isAnnotated(Inject.class);
//  }
//
//  @Override
//  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
//    final Parameter parameter = parameterContext.getParameter();
//    final Inject inject = parameter.getAnnotation(Inject.class);
//    if (inject == null) {
//      throw new IllegalStateException("Expected @Inject? on parameter " + parameter.getName());
//    }
//    final String name = qualifierName(parameter);
//    final BeanScope beanScope = (BeanScope) context.getStore(INJECT_NS).get(BEAN_SCOPE);
//    return beanScope.get(parameter.getType(), name);
//  }
//
//  private String qualifierName(Parameter parameter) {
//    final Named named = parameter.getAnnotation(Named.class);
//    if (named != null) {
//      return named.value().toLowerCase();
//    }
//    for (Annotation annotation : parameter.getAnnotations()) {
//      if (annotation.annotationType().equals(Qualifier.class)) {
//        return annotation.annotationType().getSimpleName().toLowerCase();
//      }
//    }
//    return null;
//  }
}
