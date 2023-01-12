package io.avaje.inject.test;

import io.avaje.applog.AppLog;
import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.lang.System.Logger.Level;
import java.lang.reflect.Method;

/**
 * Junit 5 extension for avaje inject.
 * <p>
 * Supports injection for fields annotated with <code>@Mock, @Spy, @Captor, @Inject</code>.
 */
public final class InjectExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

  private static final System.Logger log = AppLog.getLogger("io.avaje.inject");
  private static final Namespace INJECT_NS = Namespace.create("io.avaje.inject.InjectTest");
  private static final String BEAN_SCOPE = "BEAN_SCOPE";
  private static final String META = "META";
  private static final GlobalTestScope GLOBAL = new GlobalTestScope();

  private BeanScope globalBeanScope;

  @Override
  public void beforeAll(ExtensionContext context) {
    globalBeanScope = GLOBAL.obtain(context);

    final MetaInfo metaInfo = createMetaInfo(context);
    putMetaInfo(context, metaInfo);
    if (metaInfo.hasStaticInjection()) {
      putClassScope(context, metaInfo.buildForClass(globalBeanScope));
    }
  }

  private void putMetaInfo(ExtensionContext context, MetaInfo metaInfo) {
    Class<?> testClass = context.getRequiredTestClass();
    context.getStore(INJECT_NS).put(META + testClass, metaInfo);
  }

  private MetaInfo getMetaInfo(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    return (MetaInfo) context.getStore(INJECT_NS).get(META + testClass);
  }

  private void putClassScope(ExtensionContext context, MetaInfo.Scope testClassBeanScope) {
    Class<?> testClass = context.getRequiredTestClass();
    context.getStore(INJECT_NS).put(BEAN_SCOPE + testClass, testClassBeanScope);
  }

  private BeanScope getClassScope(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    MetaInfo.Scope pair = (MetaInfo.Scope) context.getStore(INJECT_NS).get(BEAN_SCOPE + testClass);
    return pair.beanScope();
  }

  /**
   * Callback that is invoked <em>before</em> each test is invoked.
   */
  @Override
  public void beforeEach(final ExtensionContext context) {
    final MetaInfo metaInfo = getMetaInfo(context);
    if (metaInfo.hasInstanceInjection()) {

      // if (static fields) then (class scope) else (globalTestScope)
      final BeanScope parent = metaInfo.hasStaticInjection() ? getClassScope(context) : globalBeanScope;

      AutoCloseable beanScope = metaInfo.buildForInstance(parent, context.getRequiredTestInstance());

      // put method level test scope
      Method testMethod = context.getRequiredTestMethod();
      context.getStore(INJECT_NS).put(BEAN_SCOPE + testMethod, beanScope);
    }
  }

  /**
   * Cleanup scope for test method.
   */
  @Override
  public void afterEach(ExtensionContext context) {
    Method testMethod = context.getRequiredTestMethod();
    final AutoCloseable beanScope = (AutoCloseable) context.getStore(INJECT_NS).remove(BEAN_SCOPE + testMethod);
    if (beanScope != null) {
      try {
        beanScope.close();
      } catch (Exception e) {
        log.log(Level.ERROR, "Error closing scope", e);
      }
    }
  }

  /**
   * Cleanup scope for class (static fields).
   */
  @Override
  public void afterAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    final AutoCloseable scope = (AutoCloseable) context.getStore(INJECT_NS).remove(BEAN_SCOPE + testClass);
    if (scope != null) {
      try {
        scope.close();
      } catch (Exception e) {
        log.log(Level.ERROR, "Error closing scope", e);
      }
    }
  }

  /**
   * Return the MetaInfo.
   */
  private MetaInfo createMetaInfo(ExtensionContext context) {
    return new MetaInfo(context.getRequiredTestClass(), PluginInitialise.plugin());
  }

}
