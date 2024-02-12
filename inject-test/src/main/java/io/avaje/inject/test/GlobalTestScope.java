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
  private BeanScope globalBeanScope;

  BeanScope obtain(ExtensionContext context) {
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
    globalBeanScope = TestBeanScope.init(false);
    if (globalBeanScope != null) {
      log.log(TRACE, "register global test BeanScope with beans {0}", globalBeanScope);
      context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put(InjectExtension.class.getCanonicalName(), this);
    }
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

}
