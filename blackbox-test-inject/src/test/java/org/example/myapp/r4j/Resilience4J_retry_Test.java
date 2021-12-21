package org.example.myapp.r4j;

import io.avaje.inject.BeanScope;
import io.avaje.inject.aop.InvocationException;
import org.example.myapp.resilience4j.RetryProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Resilience4J_retry_Test {

  @Test
  void retry_test() {

    MyExample myExample;
    try (BeanScope beanScope = BeanScope.newBuilder().build()) {
      myExample = beanScope.get(MyExample.class);

      assertThatThrownBy(myExample::doingItWithRetry)
        .isInstanceOf(InvocationException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasMessage("java.lang.IllegalArgumentException: no");
    }

    assertThat(myExample.barfCounter).isEqualTo(3);
    assertThat(RetryProvider.testCounter.getAndSet(0)).isEqualTo(1);
  }

  @Test
  void retry_fallback_test() {

    MyExample myExample;
    try (BeanScope beanScope = BeanScope.newBuilder().build()) {
      myExample = beanScope.get(MyExample.class);

      String result = myExample.retryWithFallback();
      assertThat(result).isEqualTo("fallback-response");
    }

    assertThat(myExample.retryWithFallbackCounter).isEqualTo(5);
    assertThat(RetryProvider.testCounter.getAndSet(0)).isEqualTo(1);
  }

  @Test
  void retry_fallback_throwable_test() {

    MyExample myExample;
    try (BeanScope beanScope = BeanScope.newBuilder().build()) {
      myExample = beanScope.get(MyExample.class);

      String result = myExample.retry2("foo", 45);
      assertThat(result).isEqualTo("fallbackRetry2-foo:45:Retry2Fail[foo,45]");
    }

    assertThat(myExample.retryWithFallbackCounter).isEqualTo(3);
    assertThat(RetryProvider.testCounter.getAndSet(0)).isEqualTo(1);
  }
}
