package org.multi.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.other.one.OtherComponent;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Back on the global scope after T6 and T7 wired bounded scopes. */
@InjectTest
class T8GlobalAgainTest {

  @Inject RegistryBean bean;
  @Inject OtherComponent otherComponent;

  @Test
  void resourceStillPresent() {
    assertNotNull(bean.lookup());
    assertNotNull(otherComponent);
  }
}
