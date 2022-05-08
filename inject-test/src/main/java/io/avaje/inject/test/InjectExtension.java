package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Junit 5 extension for avaje inject.
 * <p>
 * Supports injection for fields annotated with <code>@Mock, @Spy, @Captor, @Inject</code>.
 */
public class InjectExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ExtensionContext.Store.CloseableResource {

  private static final System.Logger log = System.getLogger("io.avaje.inject");
  private static final Namespace INJECT_NS = Namespace.create("io.avaje.inject.InjectTest");
  private static final String BEAN_SCOPE = "BEAN_SCOPE";
  private static final String META = "META";
  private static final ReentrantLock lock = new ReentrantLock();
  private static boolean started;
  private static BeanScope globalTestScope;

  /**
   * Global shutdown of JUnit.
   */
  @Override
  public void close() {
    lock.lock();
    try {
      if (globalTestScope != null) {
        log.log(Level.DEBUG, "Closing global test BeanScope");
        globalTestScope.close();
      }
    } finally {
      lock.unlock();
    }
  }

  private void initialiseGlobalTestScope(ExtensionContext context) {
    globalTestScope = TestBeanScope.init(false);
    if (globalTestScope != null) {
      log.log(Level.TRACE, "register global test BeanScope with beans {0}", globalTestScope);
      context.getRoot().getStore(Namespace.GLOBAL).put(InjectExtension.class.getCanonicalName(), this);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    lock.lock();
    try {
      if (!started) {
        initialiseGlobalTestScope(context);
        started = true;
      }

      final MetaInfo metaInfo = createMetaInfo(context);
      putMetaInfo(context, metaInfo);

      if (metaInfo.hasStaticInjection()) {
        BeanScope testClassBeanScope = metaInfo.buildForClass(globalTestScope);
        putClassScope(context, testClassBeanScope);
      }
    } finally {
      lock.unlock();
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

  private void putClassScope(ExtensionContext context, BeanScope testClassBeanScope) {
    Class<?> testClass = context.getRequiredTestClass();
    context.getStore(INJECT_NS).put(BEAN_SCOPE + testClass, testClassBeanScope);
  }

  private BeanScope getClassScope(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    return (BeanScope) context.getStore(INJECT_NS).get(BEAN_SCOPE + testClass);
  }

  /**
   * Callback that is invoked <em>before</em> each test is invoked.
   */
  @Override
  public void beforeEach(final ExtensionContext context) {
    final MetaInfo metaInfo = getMetaInfo(context);
    if (metaInfo.hasNormalInjection()) {

      // if (static fields) then (class scope) else (globalTestScope)
      final BeanScope parent = metaInfo.hasStaticInjection() ? getClassScope(context) : globalTestScope;

      BeanScope beanScope = metaInfo.buildForInstance(parent, context.getRequiredTestInstance());

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
    final BeanScope beanScope = (BeanScope) context.getStore(INJECT_NS).remove(BEAN_SCOPE + testMethod);
    if (beanScope != null) {
      beanScope.close();
    }
  }

  /**
   * Cleanup scope for class (static fields).
   */
  @Override
  public void afterAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    final BeanScope testClassBeanScope = (BeanScope) context.getStore(INJECT_NS).remove(BEAN_SCOPE + testClass);
    if (testClassBeanScope != null) {
      testClassBeanScope.close();
    }
  }

  /**
   * Return the MetaInfo.
   */
  private MetaInfo createMetaInfo(ExtensionContext context) {
    return new MetaInfo(context.getRequiredTestClass());
  }

}
