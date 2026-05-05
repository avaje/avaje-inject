package org.example.myapp.interweave;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;
import org.other.one.interweave.BeanFromOther;
import org.other.one.interweave.BeanRequiresLocal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates that with {@code strictWiring = true}, a Maven module (blackbox-other) can
 * simultaneously <em>provide</em> beans to this module and <em>require</em> beans from this
 * module — as long as the bean-level dependency graph contains no cycles.
 *
 * <p>The dependency graph is:
 * <pre>
 *   [external]  BeanFromOther       (no deps)
 *   [local]     LocalImpl           (no deps, implements IFromLocal)
 *   [external]  BeanRequiresLocal   → IFromLocal   ← external bean needs a local bean
 *   [local]     LocalConsumer       → BeanFromOther + BeanRequiresLocal
 * </pre>
 *
 * <p>With interweaving the generated {@code MyappModule.build()} emits all four in exactly
 * that order, so every dependency is satisfied at the point of construction.
 */
class BidirectionalInterweaveTest {

  @Test
  void externalModuleProvidesAndRequires() {
    try (BeanScope scope = BeanScope.builder().build()) {

      // External module provides BeanFromOther with no local dependencies.
      BeanFromOther beanFromOther = scope.get(BeanFromOther.class);
      assertNotNull(beanFromOther);

      // External module's BeanRequiresLocal depends on IFromLocal, which is
      // implemented by LocalImpl in this module. Interleaving ensures LocalImpl
      // is built before BeanRequiresLocal even though both live in different modules.
      BeanRequiresLocal beanRequiresLocal = scope.get(BeanRequiresLocal.class);
      assertNotNull(beanRequiresLocal);
      assertInstanceOf(LocalImpl.class, beanRequiresLocal.iFromLocal());

      // LocalConsumer depends on both external beans — verifies the full chain.
      LocalConsumer consumer = scope.get(LocalConsumer.class);
      assertSame(beanFromOther, consumer.fromOther());
      assertSame(beanRequiresLocal, consumer.requiresLocal());
    }
  }
}
