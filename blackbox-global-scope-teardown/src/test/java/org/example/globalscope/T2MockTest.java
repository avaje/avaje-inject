package org.example.globalscope;

import io.avaje.inject.test.InjectTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Never references ServerStatus. Adding one mock of an unrelated interface rebuilds the
 * entire application beside the live global scope, so ServerStatus is constructed a second
 * time and its duplicate MBean registration fails the wiring.
 */
@InjectTest
class T2MockTest {

  @Mock Greeter greeter;

  @Test
  void mockGreeter() {
    when(greeter.greet()).thenReturn("mocked");
    assertEquals("mocked", greeter.greet());
  }
}
