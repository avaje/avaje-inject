package org.example.myapp.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class LazyTest {

  @Test
  void test() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      LazyBean lazy = scope.get(LazyBean.class, "single");
      assertThat(initialized).isTrue();
      assertThat(lazy).isNotNull();

      LazyBean lazyAgain = scope.get(LazyBean.class, "single");
      assertThat(lazyAgain).isSameAs(lazy);
    }
  }

  @Test
  void testFactory() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      LazyBean prov = scope.get(LazyBean.class, "factory");
      assertThat(initialized).isTrue();
      assertThat(prov).isNotNull();
    }
  }
}
