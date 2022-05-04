package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Junit 5 extension for avaje inject.
 * <p>
 * Supports injection for fields annotated with <code>@Mock, @Spy, @Captor, @Inject</code>.
 */
public class InjectExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ExtensionContext.Store.CloseableResource {

  private static final System.Logger log = System.getLogger("io.avaje.inject");
  private static final Namespace INJECT_NS = Namespace.create("io.avaje.inject.InjectTest");
  private static final String BEAN_SCOPE = "BEAN_SCOPE";
  private static final ReentrantLock lock = new ReentrantLock();
  private static boolean started;
  private static BeanScope globalTestScope;

  @Override
  public void beforeAll(ExtensionContext context) {
    lock.lock();
    try {
      if (!started) {
        initialiseGlobalTestScope(context);
        started = true;
      }
    } finally {
      lock.unlock();
    }
  }

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

  /**
   * Callback that is invoked <em>before</em> each test is invoked.
   */
  @Override
  public void beforeEach(final ExtensionContext context) {
    final List<MetaReader> readers = createMetaReaders(context);
    final BeanScopeBuilder builder = BeanScope.builder();
    if (globalTestScope != null) {
      builder.parent(globalTestScope, false);
    }
    // register mocks and spies local to this test
    for (MetaReader reader : readers) {
      reader.build(builder);
    }

    // wire with local mocks, spies, and globalTestScope
    final BeanScope beanScope = builder.build();
    for (MetaReader reader : readers) {
      reader.setFromScope(beanScope);
    }
    log.log(Level.TRACE, "test setup with {0}", readers);
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

}
