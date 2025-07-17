package io.avaje.inject.test;

import java.util.Optional;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;

/**
 * Wraps the underlying metadata (fields with annotations @Mock, @Spy, @Inject, @Captor).
 */
final class MetaInfo {

  private final MetaReader reader;

  MetaInfo(Class<?> testClass, Plugin plugin) {
    this.reader = new MetaReader(testClass, plugin);
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
    var injectTest =
        Optional.ofNullable(testInstance)
            .map(Object::getClass)
            .map(c -> c.getAnnotation(InjectTest.class));

    // wiring profiles
    String[] profiles = injectTest.map(InjectTest::profiles).orElse(new String[0]);

    if (profiles.length > 0
        || injectTest.map(InjectTest::refreshScope).orElse(false)
        || reader.hasMocksOrSpies(testInstance)) {
      // need to build a BeanScope for this using baseBeans() as the parent
      final BeanScopeBuilder builder = BeanScope.builder();
      if (parent != null) {
        builder.parent(parent.baseBeans(), false);
        if (profiles.length > 0) {
          builder.profiles(profiles);
        }
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
