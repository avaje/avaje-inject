package org.example.myapp.async;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class AsyncTest {

  @Test
  void test() {
    try (var scope = BeanScope.builder().build()) {
      var lazy = scope.get(BackgroundBean.class, "single");
      assertThat(lazy).isNotNull();

      var lazyAgain = scope.get(BackgroundBean.class, "single");
      assertThat(lazyAgain).isSameAs(lazy);
    }
  }

  @Test
  void testFactory() {
    try (var scope = BeanScope.builder().build()) {
      var prov = scope.get(BackgroundBean.class, "factory");
      assertThat(prov).isNotNull();
    }
  }
}
