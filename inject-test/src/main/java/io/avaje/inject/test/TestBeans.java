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
  /** Non-null when we are sharing the application scope owned by these beans. */
  private final GlobalTestBeans.Beans shared;

  /**
   * Create with new beans and plugin which will be closed at the end of the test.
   */
  TestBeans(BeanScope beanScope, Plugin.Scope pluginScope) {
    this.beanScope = beanScope;
    this.pluginScope = pluginScope;
    this.closeAtEndOfTest = true;
    this.shared = null;
  }

  /**
   * Create with existing beans - nothing closed at the end of the test.
   * <p>
   * The shared application scope is held (and so protected from being closed and
   * re-created) until this is closed at the end of the test.
   */
  TestBeans(GlobalTestBeans.Beans parent) {
    this.beanScope = parent.allBeans();
    this.pluginScope = parent.allPlugin();
    this.closeAtEndOfTest = false;
    this.shared = parent;
    parent.acquire();
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
    } else if (shared != null) {
      shared.release();
    }
  }
}
