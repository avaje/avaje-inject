package io.avaje.inject.test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.BeanScopeBuilder;

/**
 * Wraps the underlying metadata (fields with annotations @Mock, @Spy, @Inject, @Captor).
 */
class MetaInfo {

  private final MetaReader reader;

  MetaInfo(Class<?> testClass) {
    this.reader = new MetaReader(testClass);
  }

  boolean hasStaticInjection() {
    return reader.hasClassInjection();
  }

  boolean hasNormalInjection() {
    return reader.hasInstanceInjection();
  }

  /**
   * Build for static fields class level scope.
   */
  BeanScope buildForClass(BeanScope globalTestScope) {
    return buildSet(globalTestScope, null);
  }

  /**
   * Build test instance per test scope.
   */
  BeanScope buildForInstance(BeanScope globalTestScope, Object testInstance) {
    return buildSet(globalTestScope, testInstance);
  }

  private BeanScope buildSet(BeanScope parent, Object testInstance) {
    final BeanScopeBuilder builder = BeanScope.builder();
    if (parent != null) {
      builder.parent(parent, false);
    }
    // register mocks and spies local to this test
    reader.build(builder, testInstance);

    // wire with local mocks, spies, and globalTestScope
    final BeanScope beanScope = builder.build();

    // set inject, spy, mock fields from beanScope
    reader.setFromScope(beanScope, testInstance);
    return beanScope;
  }
}
