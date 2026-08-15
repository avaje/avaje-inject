package org.multi.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@InjectTest
class T1GlobalTest {

  @Inject RegistryBean bean;

  @Test
  void resourcePresent() {
    assertNotNull(bean.lookup());
  }
}
