package io.avaje.inject.test;

import io.avaje.applog.AppLog;
import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.AvajeModule;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.Closeable;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Holds the global BeanScope used for all tests.
 */
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

    private final ReentrantLock allBeansLock = new ReentrantLock();

    private Plugin.Scope plugin;

    /**
     * Entire application wired (with testScope as parent replacing those beans).
     * This can be used when a test only injects beans and there are no mocks,
     * spies, or setup methods.
     * <p>
     * Wired lazily on first use as a test using {@code @InjectTest(modules = ...)}
     * only wires the modules it selects and never uses this.
     * <p>
     * When {@code @TestModules} is specified, only those modules are wired.
     */
    private BeanScope allBeans;

    /**
     * The TestScope beans, used as the parent scope when a new BeanScope
     * needs to be wired for a test (due to mocks, spies or setup methods).
     */
    private final BeanScope baseBeans;

    /**
     * The default modules to wire as specified via {@code @TestModules}. When empty,
     * all the modules on the classpath are wired.
     */
    private final Class<? extends AvajeModule>[] defaultModules;

    Beans(BeanScope baseBeans, Class<? extends AvajeModule>[] defaultModules) {
      this.baseBeans = baseBeans;
      this.defaultModules = defaultModules;
    }

    Beans(Plugin.Scope plugin, BeanScope allBeans, BeanScope baseBeans, Class<? extends AvajeModule>[] defaultModules) {
      this.plugin = plugin;
      this.allBeans = allBeans;
      this.baseBeans = baseBeans;
      this.defaultModules = defaultModules;
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
      allBeansLock.lock();
      try {
        initAllBeans();
        return plugin;
      } finally {
        allBeansLock.unlock();
      }
    }

    BeanScope allBeans() {
      allBeansLock.lock();
      try {
        initAllBeans();
        return allBeans;
      } finally {
        allBeansLock.unlock();
      }
    }

    private void initAllBeans() {
      if (allBeans == null) {
        log.log(DEBUG, "Wiring all beans for the test BeanScope");
        var builder = BeanScope.builder().parent(baseBeans, false);
        if (defaultModules.length > 0) {
          builder.modules(SelectedModules.instances(defaultModules));
        }
        allBeans = builder.build();
        plugin = PluginMgr.scope(allBeans);
      }
    }

    BeanScope baseBeans() {
      return baseBeans;
    }

    Class<? extends AvajeModule>[] defaultModules() {
      return defaultModules;
    }

    Beans withBeans(TestBeans otherBeans) {
      return new Beans(otherBeans.plugin(), otherBeans.beanScope(), baseBeans, defaultModules);
    }

    @Override
    public String toString() {
      return "All[" + allBeans + "] Test[" + baseBeans + "]";
    }
  }

}
