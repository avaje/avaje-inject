package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.AvajeModule;

/** Wraps the underlying metadata (fields with annotations @Mock, @Spy, @Inject, @Captor). */
final class MetaInfo {

  private static final String[] NO_PROFILES = {};

  @SuppressWarnings("unchecked")
  private static final Class<? extends AvajeModule>[] NO_MODULES = new Class[0];

  private final MetaReader reader;
  private final String[] profiles;
  private final Class<? extends AvajeModule>[] modules;
  private final boolean scopePerTest;

  MetaInfo(Class<?> testClass, Plugin plugin) {
    this.reader = new MetaReader(testClass, plugin);
    final InjectTest injectTest = testClass.getAnnotation(InjectTest.class);
    this.profiles = injectTest == null ? NO_PROFILES : injectTest.profiles();
    this.modules = injectTest == null ? NO_MODULES : injectTest.modules();
    this.scopePerTest = injectTest != null && injectTest.scopePerTest();
  }

  boolean hasStaticInjection() {
    return reader.hasClassInjection();
  }

  boolean hasInstanceInjection() {
    return reader.hasInstanceInjection();
  }

  /** Build for static fields class level scope. */
  TestBeans buildForClass(GlobalTestBeans.Beans globalTestScope) {
    return buildSet(globalTestScope, null);
  }

  /** Build test instance per test scope. */
  TestBeans buildForInstance(GlobalTestBeans.Beans globalTestScope, Object testInstance) {
    return buildSet(globalTestScope, testInstance);
  }

  private TestBeans buildSet(GlobalTestBeans.Beans parent, Object testInstance) {
    var testBeans = buildTestBeans(parent, testInstance);
    // set inject, spy, mock fields from beanScope
    return reader.setFromScope(testBeans, testInstance);
  }

  private TestBeans buildTestBeans(GlobalTestBeans.Beans parent, Object testInstance) {
    if ((profiles.length <= 0)
        && (modules.length <= 0)
        && !scopePerTest
        && !reader.hasMocksOrSpies(testInstance)) {
      // just use the existing beans and plugin from parent
      return new TestBeans(parent);
    }
    // need to build a BeanScope for this using baseBeans() as the parent.
    // Close the shared application scope first (when nothing else is using it) so
    // that we don't run the lifecycle of every singleton a second time.
    if (parent != null) {
      parent.restartAllBeans();
    }
    final BeanScopeBuilder builder = BeanScope.builder();
    if (parent != null) {
      builder.parent(parent.baseBeans(), false);
    }
    if (profiles.length > 0) {
      builder.profiles(profiles);
    }
    if (modules.length > 0) {
      builder.modules(SelectedModules.instances(modules));
    }
    // register mocks and spies local to this test
    reader.build(builder, testInstance);
    // wire with local mocks, spies, and TestScope beans
    var newBeanScope = builder.build();
    var newPlugin = PluginMgr.scope(newBeanScope);
    return new TestBeans(newBeanScope, newPlugin);
  }
}
