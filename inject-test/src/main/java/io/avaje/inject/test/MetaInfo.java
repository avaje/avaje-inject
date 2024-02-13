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
  Scope buildForClass(GlobalTestScope.Pair globalTestScope) {
    return buildSet(globalTestScope, null);
  }

  /**
   * Build test instance per test scope.
   */
  Scope buildForInstance(GlobalTestScope.Pair globalTestScope, Object testInstance) {
    return buildSet(globalTestScope, testInstance);
  }

  private Scope buildSet(GlobalTestScope.Pair parent, Object testInstance) {
    // wiring profiles
    String[] profiles = Optional.ofNullable(testInstance)
      .map(Object::getClass)
      .map(c -> c.getAnnotation(InjectTest.class))
      .map(InjectTest::profiles)
      .orElse(new String[0]);

    boolean newScope = false;
    final BeanScope beanScope;
    if (profiles.length > 0 || reader.hasMocksOrSpies(testInstance)) {
      // need to build a BeanScope for this using testScope() as the parent
      final BeanScopeBuilder builder = BeanScope.builder();
      if (parent != null) {
        builder.parent(parent.baseScope(), false);
        if (profiles.length > 0) {
          builder.profiles(profiles);
        }
      }
      // register mocks and spies local to this test
      reader.build(builder, testInstance);
      // wire with local mocks, spies, and globalTestScope
      beanScope = builder.build();
      newScope = true;
    } else {
      // just use the all scope
      beanScope = parent.allScope();
    }

    // set inject, spy, mock fields from beanScope
    return reader.setFromScope(beanScope, testInstance, newScope);
  }

  /**
   * Wraps both BeanScope and Plugin.Scope for either EACH or ALL
   * (aka instance or class level).
   */
  static class Scope implements AutoCloseable {

    private final BeanScope beanScope;
    private final Plugin.Scope pluginScope;
    private final boolean newScope;

    Scope(BeanScope beanScope, Plugin.Scope pluginScope, boolean newScope) {
      this.beanScope = beanScope;
      this.pluginScope = pluginScope;
      this.newScope = newScope;
    }

    BeanScope beanScope() {
      return beanScope;
    }

    @Override
    public void close() {
      if (newScope) {
        beanScope.close();
      }
      if (pluginScope != null) {
        pluginScope.close();
      }
    }
  }
}
