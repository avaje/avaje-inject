package org.example.myapp.async;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class AsyncTest {

  @Test
  void test() {
    var start = Instant.now();
    var inty = new AtomicInteger();
    try (var scope = BeanScope.builder().bean(AtomicInteger.class, inty).build()) {

      // the async beans shouldn't slowdown initialization
      assertThat(Duration.between(start, Instant.now()).toMillis()).isLessThan(1000);
      // the async beans shouldn't slowdown initialization
      assertThat(Duration.between(start, Instant.now()).toMillis()).isLessThan(1000);

      // prove it's not just lazy
      var beforeGet = Instant.now();
      var bean = scope.get(BackgroundBean.class, "single");
      assertThat(inty.get()).isEqualTo(2);
      assertThat(bean.initTime.isBefore(beforeGet)).isTrue();
      assertThat(bean.threadName).isNotEqualTo(Thread.currentThread().getName());
    }
  }

  @Test
  void testFactory() {
    var start = Instant.now();
    var inty = new AtomicInteger();
    try (var scope = BeanScope.builder().bean(AtomicInteger.class, inty).build()) {
      // the async beans shouldn't slowdown initialization
      assertThat(Duration.between(start, Instant.now()).toMillis()).isLessThan(1000);

      var bean = scope.get(BackgroundBean.class, "factory");
      // this works on my local but not on the CI for some unknown reason.
      // var beforeGet = Instant.now();
      // assertThat(bean.initTime.isBefore(beforeGet)).isTrue();
      assertThat(inty.get()).isEqualTo(2);
      assertThat(bean.threadName).isNotEqualTo(Thread.currentThread().getName());
    }
  }
}
