package org.example.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@InjectTest
class T3GlobalAgainTest {
  @Inject RegistryBean bean;

  @Test
  void resourceStillPresent() {
    assertNotNull(bean.lookup());
  }
}
