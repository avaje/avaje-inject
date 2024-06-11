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
final class GlobalTestBeans implements ExtensionContext.Store.CloseableResource {

  private static final System.Logger log = AppLog.getLogger("io.avaje.inject");

  private final ReentrantLock lock = new ReentrantLock();
  private boolean started;
  private Beans globalBeans;

  Beans obtain(ExtensionContext context) {
    lock.lock();
    try {
      if (!started) {
        initialise(context);
        started = true;
      }
      return globalBeans;
    } finally {
      lock.unlock();
    }
  }

  private void initialise(ExtensionContext context) {
    globalBeans = GlobalInitialise.initialise(false);
    log.log(TRACE, "register global test BeanScope with beans {0}", globalBeans);
    context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put(InjectJunitExtension.class.getCanonicalName(), this);
  }

  /**
   * Global shutdown of JUnit.
   */
  @Override
  public void close() {
    lock.lock();
    try {
      if (globalBeans != null) {
        log.log(DEBUG, "Closing global test BeanScope");
        globalBeans.close();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * The BeanScopes and plugin scope that can be used for InjectTests.
   */
  static final class Beans {

    private final Plugin.Scope plugin;

    /**
     * Entire application wired (with testScope as parent replacing those beans).
     * This can be used when a test only injects beans and there are no mocks,
     * spies, or setup methods.
     */
    private final BeanScope allBeans;

    /**
     * The TestScope beans, used as the parent scope when a new BeanScope
     * needs to be wired for a test (due to mocks, spies or setup methods).
     */
    private final BeanScope baseBeans;

    Beans(Plugin.Scope plugin, BeanScope allBeans, BeanScope baseBeans) {
      this.plugin = plugin;
      this.allBeans = allBeans;
      this.baseBeans = baseBeans;
    }

    void close() {
      if (plugin != null) {
        plugin.close();
      }
      if (allBeans != null) {
        allBeans.close();
      }
      if (baseBeans != null) {
        baseBeans.close();
      }
    }

    Plugin.Scope allPlugin() {
      return plugin;
    }

    BeanScope allBeans() {
      return allBeans;
    }

    BeanScope baseBeans() {
      return baseBeans;
    }

    Beans withBeans(TestBeans otherBeans) {
      return new Beans(otherBeans.plugin(), otherBeans.beanScope(), baseBeans);
    }

    @Override
    public String toString() {
      return "All[" + allBeans + "] Test[" + baseBeans + "]";
    }
  }

}
