package io.avaje.inject.test;

import io.avaje.inject.BeanScope;

/**
 * Wraps both BeanScope and Plugin.Scope for either EACH or ALL
 * (aka instance or class level).
 */
final class TestBeans implements AutoCloseable {

  private final BeanScope beanScope;
  private final Plugin.Scope pluginScope;
  private final boolean closeAtEndOfTest;

  /**
   * Create with new beans and plugin which will be closed at the end of the test.
   */
  TestBeans(BeanScope beanScope, Plugin.Scope pluginScope) {
    this.beanScope = beanScope;
    this.pluginScope = pluginScope;
    this.closeAtEndOfTest = true;
  }

  /**
   * Create with existing beans - nothing closed at the end of the test.
   */
  TestBeans(GlobalTestBeans.Beans parent) {
    this.beanScope = parent.allBeans();
    this.pluginScope = parent.allPlugin();
    this.closeAtEndOfTest = false;
  }

  BeanScope beanScope() {
    return beanScope;
  }

  Plugin.Scope plugin() {
    return pluginScope;
  }

  @Override
  public void close() {
    if (closeAtEndOfTest) {
      if (pluginScope != null) {
        pluginScope.close();
      }
      beanScope.close();
    }
  }
}
