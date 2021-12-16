package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.example.myapp.aspect.MyMultiInvokeAspect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Test
  void aspect_multiInvoke() {
    BeanScope beanScope = BeanScope.newBuilder().build();

    MyMultiInvokeAspect aspect = beanScope.get(MyMultiInvokeAspect.class);
    HelloService helloService = beanScope.get(HelloService.class);

    int result = helloService.counter();
    assertThat(result).isEqualTo(4);
    assertThat(aspect.results).containsExactly(0, 1, 2, 3, 4);
  }
}
