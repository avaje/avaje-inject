package org.example.myapp.lazy.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;
import io.avaje.inject.spi.GenericType;

class LazyGenericTest {

  @Test
  void testBeanTypes() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      var lazy = scope.get(LazyGenericInterface.class, "single");
      assertThat(lazy).isNotNull();
      assertThat(initialized).isTrue();
    }
  }

  @Test
  void testFactoryInterface() {
    var initialized = new AtomicBoolean();
    try (var scope = BeanScope.builder().beans(initialized).build()) {
      assertThat(initialized).isFalse();
      LazyGenericInterface<String> prov =
          scope.get(new GenericType<LazyGenericInterface<String>>() {}.type(), "factory");
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
      var lazy = scope.get(LazyGenericInterface.class, "factoryBeanType");
      assertThat(lazy).isNotNull();
      assertThat(initialized).isTrue();
    }
  }
}
