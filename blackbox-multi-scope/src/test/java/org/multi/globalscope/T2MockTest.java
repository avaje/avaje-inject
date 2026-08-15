package org.multi.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Wires its own copy of the application because of the mock. The global scope must
 * not be left live beside it - see T3.
 */
@InjectTest
class T2MockTest {

  @Mock Greeter greeter;
  @Inject RegistryBean bean;

  @Test
  void mockedGreeter() {
    when(greeter.greet()).thenReturn("mocked");
    assertEquals("mocked", greeter.greet());
    assertNotNull(bean.lookup());
  }
}
