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
      var lazy = scope.get(LazyBean.class, "single");
      assertThat(lazy).isNotNull();
      assertThat(initialized).isFalse();
      lazy.something();
      assertThat(initialized).isTrue();

      var lazyAgain = scope.get(LazyBean.class, "single");
      assertThat(lazyAgain).isSameAs(lazy);
    }
  }

  @Test
  void testFactory() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var prov = scope.get(LazyBean.class, "factory");
      assertThat(initialized).isFalse();
      prov.something();
      assertThat(initialized).isTrue();
      assertThat(prov).isNotNull();
    }
  }

  @Test
  void testInterface() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var lazy = scope.get(LazyInterface.class, "single");
      assertThat(lazy).isNotNull();
      assertThat(initialized).isFalse();
      lazy.something();
      assertThat(initialized).isTrue();

      var lazyAgain = scope.get(LazyInterface.class, "single");
      assertThat(lazyAgain).isSameAs(lazy);
    }
  }

  @Test
  void testFactoryInterface() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var prov = scope.get(LazyInterface.class, "factory");
      assertThat(initialized).isFalse();
      prov.something();
      assertThat(initialized).isTrue();
      assertThat(prov).isNotNull();
    }
  }

  @Test
  void factoryBeanType() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var prov = scope.get(LazyInterface.class, "factoryBeanType");
      assertThat(initialized).isFalse();
      prov.something();
      assertThat(initialized).isTrue();
      assertThat(prov).isNotNull();
    }
  }

  @Test
  void testAOP() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var lazy = scope.get(LazyBeanAOP.class, "single");
      assertThat(lazy).isNotNull();
      assertThat(initialized).isFalse();
      lazy.something();
      assertThat(initialized).isTrue();

      var lazyAgain = scope.get(LazyBeanAOP.class, "single");
      assertThat(lazyAgain).isSameAs(lazy);
    }
  }

  @Test
  void testOldLazyBehavior() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var lazy = scope.get(OldLazy.class, "single");
      assertThat(lazy).isNotNull();
      assertThat(initialized).isTrue();
      lazy.something();

      var lazyAgain = scope.get(OldLazy.class, "single");
      assertThat(lazyAgain).isSameAs(lazy);
    }
  }
}
