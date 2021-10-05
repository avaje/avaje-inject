package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloServiceTest {

  /**
   * No mocking, no use of <code>@TestScope</code> so just like main.
   */
  @Test
  void basic() {
    // just wire everything with no test scope, mocks etc
    BeanScope beanScope = BeanScope.newBuilder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    assertEquals("hello+AppHelloData", helloService.hello());
  }
}
