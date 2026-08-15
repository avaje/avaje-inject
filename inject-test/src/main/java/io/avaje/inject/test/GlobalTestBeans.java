package io.avaje.inject.test;

import io.avaje.applog.AppLog;
import io.avaje.inject.BeanScope;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.Closeable;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/** Holds the global BeanScope used for all tests. */
final class GlobalTestBeans implements Closeable {

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

  /** Global shutdown of JUnit. */
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

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * The TestScope beans, used as the parent scope when a new BeanScope needs to be wired for a
     * test (due to mocks, spies or setup methods).
     */
    @Nullable private final BeanScope baseBeans;

    /**
     * Set when this is a class level view of the global beans. The application scope is then owned
     * (and shared) by the global instance.
     */
    @Nullable private final Beans owner;

    /**
     * Entire application wired (with testScope as parent replacing those beans). This can be used
     * when a test only injects beans and there are no mocks, spies, or setup methods.
     *
     * <p>Created lazily and re-created when a test class wires its own copy of the application.
     *
     * @see {@link #restartAllBeans()}.
     */
    @Nullable private BeanScope allBeans;

    private Plugin.Scope plugin;

    /** The number of test scopes currently (using) {@link #allBeans}. */
    private int sharedUsers;

    Beans(@Nullable BeanScope baseBeans) {
      this.baseBeans = baseBeans;
      this.owner = null;
    }

    private Beans(Beans owner, Plugin.Scope plugin, BeanScope allBeans, @Nullable BeanScope baseBeans) {
      this.owner = owner;
      this.plugin = plugin;
      this.allBeans = allBeans;
      this.baseBeans = baseBeans;
    }

    void close() {
      lock.lock();
      try {
        closeAllBeans();
        if (baseBeans != null) {
          baseBeans.close();
        }
      } finally {
        lock.unlock();
      }
    }

    Plugin.Scope allPlugin() {
      initAllBeans();
      return plugin;
    }

    BeanScope allBeans() {
      initAllBeans();
      return allBeans;
    }

    BeanScope baseBeans() {
      return baseBeans;
    }

    /**
     * Register that a test is now using the shared application BeanScope. While there is at least
     * one user the shared scope must not be closed.
     */
    void acquire() {
      if (owner != null) {
        owner.acquire();
        return;
      }
      lock.lock();
      try {
        sharedUsers++;
      } finally {
        lock.unlock();
      }
    }

    /** Register that a test has finished using the shared application BeanScope. */
    void release() {
      if (owner != null) {
        owner.release();
        return;
      }
      lock.lock();
      try {
        if (sharedUsers > 0) {
          sharedUsers--;
        }
      } finally {
        lock.unlock();
      }
    }

    /** Close the shared application BeanScope when nothing is currently using it. */
    void restartAllBeans() {
      if (owner != null) {
        owner.restartAllBeans();
        return;
      }
      lock.lock();
      try {
        if (allBeans == null || sharedUsers > 0) {
          // never built, already closed, or another test is still using it
          return;
        }
        log.log(DEBUG, "Closing application test BeanScope, a test is wiring its own copy");
        closeAllBeans();
      } finally {
        lock.unlock();
      }
    }

    private void initAllBeans() {
      lock.lock();
      try {
        if (allBeans == null) {
          allBeans = GlobalInitialise.createTestAllScope(baseBeans);
          plugin = PluginMgr.scope(allBeans);
        }
      } finally {
        lock.unlock();
      }
    }

    private void closeAllBeans() {
      if (plugin != null) {
        plugin.close();
        plugin = null;
      }
      if (allBeans != null) {
        allBeans.close();
        allBeans = null;
      }
    }

    Beans withBeans(TestBeans otherBeans) {
      return new Beans(this, otherBeans.plugin(), otherBeans.beanScope(), baseBeans);
    }

    @Override
    public String toString() {
      return "All[" + allBeans + "] Test[" + baseBeans + "]";
    }
  }

}
