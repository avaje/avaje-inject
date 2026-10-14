package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.AvajeModule;

/**
 * Wraps the underlying metadata (fields with annotations @Mock, @Spy, @Inject, @Captor).
 */
final class MetaInfo {

  @SuppressWarnings("unchecked")
  private static final Class<? extends AvajeModule>[] NO_MODULES = new Class[0];

  private final MetaReader reader;
  private final Class<? extends AvajeModule>[] modules;
  private final String[] profiles;
  private final boolean scopePerTest;

  MetaInfo(Class<?> testClass, Plugin plugin) {
    this.reader = new MetaReader(testClass, plugin);
    // read from the test class (walking enclosing classes for @Nested tests) rather than
    // the instance so that the selected modules also bound the class level scope of a test
    // that only has static injection
    final InjectTest injectTest = injectTestAnnotation(testClass);
    this.modules = injectTest == null ? NO_MODULES : injectTest.modules();
    this.profiles = injectTest == null ? new String[0] : injectTest.profiles();
    this.scopePerTest = injectTest != null && injectTest.scopePerTest();
  }

  private static InjectTest injectTestAnnotation(Class<?> testClass) {
    for (Class<?> c = testClass; c != null; c = c.getEnclosingClass()) {
      final InjectTest injectTest = c.getAnnotation(InjectTest.class);
      if (injectTest != null) {
        return injectTest;
      }
    }
    return null;
  }

  boolean hasStaticInjection() {
    return reader.hasClassInjection();
  }

  boolean hasInstanceInjection() {
    return reader.hasInstanceInjection();
  }

  /**
   * Build for static fields class level scope.
   */
  TestBeans buildForClass(GlobalTestBeans.Beans globalTestScope) {
    return buildSet(globalTestScope, null);
  }

  /**
   * Build test instance per test scope.
   */
  TestBeans buildForInstance(GlobalTestBeans.Beans globalTestScope, Object testInstance) {
    return buildSet(globalTestScope, testInstance);
  }

  private TestBeans buildSet(GlobalTestBeans.Beans parent, Object testInstance) {
    var testBeans = buildTestBeans(parent, testInstance);
    // set inject, spy, mock fields from beanScope
    return reader.setFromScope(testBeans, testInstance);
  }

  private TestBeans buildTestBeans(GlobalTestBeans.Beans parent, Object testInstance) {
    if (profiles.length > 0
        || modules.length > 0
        || scopePerTest
        || reader.hasMocksOrSpies(testInstance)) {
      // need to build a BeanScope for this using baseBeans() as the parent
      final BeanScopeBuilder builder = BeanScope.builder();
      if (parent != null) {
        builder.parent(parent.baseBeans(), false);
        if (profiles.length > 0) {
          builder.profiles(profiles);
        }
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

    } else {
      // just use the existing beans and plugin from parent
      return new TestBeans(parent);
    }
  }

}
