package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.AvajeModule;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Internal helper to build the test scope BeanScope.
 * <p>
 * Takes into when Service loading does not work such as when using module-path.
 * In that case loads the META-INF services resources and uses reflection.
 */
final class GlobalInitialise {

  private static final ReentrantLock lock = new ReentrantLock();
  private static GlobalTestBeans.Beans SCOPE;

  private final boolean shutdownHook;

  /**
   * Create and return the test BeanScope. A BeanScope is created each
   * time this method is called.
   */
  @Nullable
  static BeanScope createTestBaseScope(boolean shutdownHook) {
    return new GlobalInitialise(shutdownHook).build();
  }

  /**
   * Return the test BeanScope only creating once.
   */
  static GlobalTestBeans.Beans initialise(boolean shutdownHook) {
    lock.lock();
    try {
      if (SCOPE == null) {
        SCOPE = createScopes(shutdownHook);
      }
      return SCOPE;
    } finally {
      lock.unlock();
    }
  }

  GlobalInitialise(boolean shutdownHook) {
    this.shutdownHook = shutdownHook;
  }

  private static GlobalTestBeans.Beans createScopes(boolean shutdownHook) {
    BeanScope testBaseScope = createTestBaseScope(shutdownHook);
    BeanScope testAllScope = createTestAllScope(testBaseScope);
    Plugin.Scope pluginAll = PluginMgr.scope(testAllScope);
    return new GlobalTestBeans.Beans(pluginAll, testAllScope, testBaseScope);
  }

  private static BeanScope createTestAllScope(BeanScope testBaseScope) {
      return BeanScope.builder()
        .parent(testBaseScope, false)
        .build();
  }

  @Nullable
  private BeanScope build() {
    List<TestModule> testModules = new ArrayList<>();
    for (TestModule next : ServiceLoader.load(TestModule.class)) {
      testModules.add(next);
    }
    if (testModules.isEmpty()) {
      return buildFromResources();
    } else {
      return buildFromModules(testModules);
    }
  }

  private BeanScope buildFromModules(List<TestModule> testModules) {
    return BeanScope.builder()
      .modules(testModules.toArray(AvajeModule[]::new))
      .shutdownHook(shutdownHook)
      .build();
  }

  /**
   * Fallback when ServiceLoader does not work in module-path for generated test service.
   */
  @Nullable
  private BeanScope buildFromResources() {
    try {
      Set<Class<?>> testModuleClasses = new LinkedHashSet<>();
      Enumeration<URL> urls = ClassLoader.getSystemResources("META-INF/services/io.avaje.inject.test.TestModule");
      while (urls.hasMoreElements()) {
        String className = readServiceClassName(urls.nextElement());
        if (className != null) {
          testModuleClasses.add(Class.forName(className));
        }
      }
      List<TestModule> testModules = new ArrayList<>();
      for (Class<?> cls : testModuleClasses) {
        testModules.add((TestModule) cls.getDeclaredConstructor().newInstance());
      }
      return testModules.isEmpty() ? null : buildFromModules(testModules);
    } catch (Throwable e) {
      throw new RuntimeException("Error trying to create TestModule", e);
    }
  }

  @Nullable
  private String readServiceClassName(URL url) throws IOException {
    if (url != null) {
      InputStream is = url.openStream();
      if (is != null) {
        try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(is))) {
          return lineNumberReader.readLine();
        }
      }
    }
    return null;
  }

}
