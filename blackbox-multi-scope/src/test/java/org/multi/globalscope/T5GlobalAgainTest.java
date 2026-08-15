package org.multi.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Back on the global scope after T4 tore down its own copy of the application. */
@InjectTest
class T5GlobalAgainTest {

  @Inject RegistryBean bean;

  @Test
  void resourceStillPresent() {
    assertNotNull(bean.lookup());
  }
}
