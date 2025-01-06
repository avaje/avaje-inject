package org.example.myapp;

import io.avaje.inject.BeanScope;
import io.avaje.inject.aop.InvocationException;
import org.example.myapp.aspect.MyAroundAspect;
import org.example.myapp.aspect.MyMultiInvokeAspect;
import org.example.myapp.config.AFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloServiceTest {

  @Test
  void lifecycles() {
    MyNestedDestroy.reset();
    AFactory.reset();
    try (BeanScope beanScope = BeanScope.builder().build()) {
      assertThat(beanScope.get(MyNestedDestroy.class)).isNotNull();
      assertThat(MyNestedDestroy.STARTED.get()).isEqualTo(1);
      assertThat(MyNestedDestroy.STOPPED.get()).isEqualTo(0);
      assertThat(AFactory.DESTROY_COUNT_BEAN.get()).isEqualTo(0);
      assertThat(AFactory.DESTROY_COUNT_AFOO.get()).isEqualTo(0);
      assertThat(AFactory.DESTROY_COUNT_COMPONENT.get()).isEqualTo(0);
    }
    assertThat(MyNestedDestroy.STOPPED.get()).isEqualTo(1);
    assertThat(AFactory.DESTROY_COUNT_BEAN.get()).isEqualTo(1);
    assertThat(AFactory.DESTROY_COUNT_AFOO.get()).isEqualTo(1);
    assertThat(AFactory.DESTROY_COUNT_COMPONENT.get()).isEqualTo(1);
  }

  /**
   * No mocking, no use of <code>@TestScope</code> so just like main.
   */
  @Test
  void basic() throws IOException {
    // just wire everything with no test scope, mocks etc
    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    assertEquals("bazz foo 42", helloService.bazz("foo", 42));
    assertEquals("hello+AppHelloData", helloService.hello());
  }

  @Test
  void skip() {
    // just wire everything with no test scope, mocks etc
    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    String result = helloService.skipExample("echo me back");
    assertEquals("my-skip-result", result);
  }

  @Test
  void aspect_multiInvoke() {
    BeanScope beanScope = BeanScope.builder().build();

    MyMultiInvokeAspect aspect = beanScope.get(MyMultiInvokeAspect.class);
    HelloService helloService = beanScope.get(HelloService.class);

    int result = helloService.counter();
    assertThat(result).isEqualTo(4);
    assertThat(aspect.results).containsExactly(0, 1, 2, 3, 4);
  }

  @Test
  void aspect_checkedRunnable() throws IOException, ClassNotFoundException {
    BeanScope beanScope = BeanScope.builder().build();

    MyAroundAspect aspect = beanScope.get(MyAroundAspect.class);
    HelloService helloService = beanScope.get(HelloService.class);

    helloService.justRun("foo", 23, 45);
    assertThat(helloService.justRunResult()).isEqualTo("foo 23 45");
    assertThat(aspect.trace()).containsExactly("justRun args:[foo, 23, 45]");

    helloService.justRun("bar", 98, 99);
    assertThat(helloService.justRunResult()).isEqualTo("bar 98 99");
    assertThat(aspect.trace()).containsExactly("justRun args:[bar, 98, 99]");
  }

  @Test
  void aspect_throwingUndeclaredException_expect_InvocationException() {
    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);
    assertThatThrownBy(helloService::thisWillThrow)
      .isInstanceOf(ArithmeticException.class)
      .hasSuppressedException(new InvocationException("thisWillThrow proxy threw exception"))
      .hasMessage("my interceptor throws this");
  }

  @Test
  void aspect_appCodeThrowingUnchecked_expect_InvocationException() {
    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);

    assertThatThrownBy(helloService::appCodeThrowsUnchecked)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("appCodeUnchecked");
  }

  @Test
  void aspect_appCodeThrowingDeclared_expect_declaredException() {
    BeanScope beanScope = BeanScope.builder().build();

    HelloService helloService = beanScope.get(HelloService.class);

    assertThatThrownBy(helloService::appCodeThrowsDeclared)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("appCodeDeclared");
  }

}
