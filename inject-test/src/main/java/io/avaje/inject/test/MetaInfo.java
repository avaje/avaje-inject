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
  Scope buildForClass(BeanScope globalTestScope) {
    return buildSet(globalTestScope, null);
  }

  /**
   * Build test instance per test scope.
   */
  Scope buildForInstance(BeanScope globalTestScope, Object testInstance) {
    return buildSet(globalTestScope, testInstance);
  }

  private Scope buildSet(BeanScope parent, Object testInstance) {
    final BeanScopeBuilder builder = BeanScope.builder();
    if (parent != null) {
      builder.parent(parent, false);
    }

    final var profiles =
        Optional.ofNullable(testInstance)
            .map(Object::getClass)
            .map(c -> c.getAnnotation(InjectTest.class))
            .map(InjectTest::profiles)
            .map(p -> String.join(",", p))
            .orElse("");

    if (!profiles.isBlank()) {
      builder.profiles(profiles);
    }
    // register mocks and spies local to this test
    reader.build(builder, testInstance);

    // wire with local mocks, spies, and globalTestScope
    final BeanScope beanScope = builder.build();

    // set inject, spy, mock fields from beanScope
    return reader.setFromScope(beanScope, testInstance);
  }

  /**
   * Wraps both BeanScope and Plugin.Scope for either EACH or ALL
   * (aka instance or class level).
   */
  static class Scope implements AutoCloseable {

    private final BeanScope beanScope;
    private final Plugin.Scope pluginScope;

    Scope(BeanScope beanScope, Plugin.Scope pluginScope) {
      this.beanScope = beanScope;
      this.pluginScope = pluginScope;
    }

    BeanScope beanScope() {
      return beanScope;
    }

    @Override
    public void close() {
      beanScope.close();
      if (pluginScope != null) {
        pluginScope.close();
      }
    }
  }
}
