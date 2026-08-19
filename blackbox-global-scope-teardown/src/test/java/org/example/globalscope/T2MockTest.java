package org.example.globalscope;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@InjectTest
class T2MockTest {
  @Mock Greeter greeter;
  @Inject RegistryBean bean;

  @Test
  void mockedGreeter() {
    when(greeter.greet()).thenReturn("mocked");
    assertNotNull(bean.lookup());
  }
}
