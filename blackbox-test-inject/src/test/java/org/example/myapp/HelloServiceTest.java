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
    assertEquals("bazz foo 42", helloService.bazz("foo", 42));
    assertEquals("hello+AppHelloData", helloService.hello());
  }

  @Test
  void skip() {
    // just wire everything with no test scope, mocks etc
    BeanScope beanScope = BeanScope.newBuilder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    String result = helloService.skipExample("echo me back");
    assertEquals("my-skip-result", result);
  }
}
