package org.multi.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** scopePerTest also wires its own copy of the application - see T5. */
@InjectTest(scopePerTest = true)
class T4ScopePerTest {

  @Inject RegistryBean bean;

  @Test
  void one() {
    assertNotNull(bean.lookup());
  }

  @Test
  void two() {
    assertNotNull(bean.lookup());
  }
}
