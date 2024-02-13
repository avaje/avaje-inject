package io.avaje.inject.test;

import io.avaje.applog.AppLog;
import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Holds the global BeanScope used for all tests.
 */
final class GlobalTestScope implements ExtensionContext.Store.CloseableResource {

  private static final System.Logger log = AppLog.getLogger("io.avaje.inject");

  private final ReentrantLock lock = new ReentrantLock();
  private boolean started;
  private Pair globalBeanScope;

  Pair obtain(ExtensionContext context) {
    lock.lock();
    try {
      if (!started) {
        initialise(context);
        started = true;
      }
      return globalBeanScope;
    } finally {
      lock.unlock();
    }
  }

  private void initialise(ExtensionContext context) {
    globalBeanScope = TSBuild.initialise(false);
    log.log(TRACE, "register global test BeanScope with beans {0}", globalBeanScope);
    context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put(InjectExtension.class.getCanonicalName(), this);
  }

  /**
   * Global shutdown of JUnit.
   */
  @Override
  public void close() {
    lock.lock();
    try {
      if (globalBeanScope != null) {
        log.log(DEBUG, "Closing global test BeanScope");
        globalBeanScope.close();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * The pair of BeanScopes that can be used for InjectTests.
   */
  static final class Pair {

    /**
     * Entire application wired (with testScope as parent replacing those beans).
     * This can be used when a test only injects beans and there are no mocks,
     * spies, or setup methods.
     */
    private final BeanScope allScope;

    /**
     * The TestScope beans, used as the parent scope when a new BeanScope
     * needs to be wired for a test (due to mocks, spies or setup methods).
     */
    private final BeanScope baseScope;

    Pair(BeanScope allScope, BeanScope baseScope) {
      this.allScope = allScope;
      this.baseScope = baseScope;
    }

    void close() {
      if (allScope != null) {
        allScope.close();
      }
      if (baseScope != null) {
        baseScope.close();
      }
    }

    BeanScope allScope() {
      return allScope;
    }

    BeanScope baseScope() {
      return baseScope;
    }

    Pair newPair(BeanScope newAllScope) {
      return new Pair(newAllScope, baseScope);
    }

    @Override
    public String toString() {
      return "All[" + allScope + "] Test[" + baseScope + "]";
    }

  }

}
