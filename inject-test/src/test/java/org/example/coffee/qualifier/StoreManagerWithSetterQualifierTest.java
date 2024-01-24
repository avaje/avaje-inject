package org.example.coffee.qualifier;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoreManagerWithSetterQualifierTest {

  @Test
  void redStore() {
    try (BeanScope context = BeanScope.builder().build()) {
      StoreManagerWithSetterQualifier manager = context.get(StoreManagerWithSetterQualifier.class);
      assertThat(manager.blueStore()).isEqualTo("blue");
      assertThat(manager.greenStore()).isEqualTo("green");
    }
  }

  @Test
  void namedTestDouble() {
    try (BeanScope context = BeanScope.builder()
      .bean("ColorStore(Blue)", SomeStore.class, () -> "TD Blue")
      .bean("Green", SomeStore.class, () -> "TD Green")
      .build()) {

      StoreManagerWithSetterQualifier manager = context.get(StoreManagerWithSetterQualifier.class);
      assertThat(manager.blueStore()).isEqualTo("TD Blue");
      assertThat(manager.greenStore()).isEqualTo("TD Green");
    }
  }

  @Test
  void namedTestDouble_expect_otherNamedStillWired() {
    try (BeanScope context = BeanScope.builder()
      .bean("ColorStore(Blue)", SomeStore.class, () -> "TD Blue Only")
      // with GreenStore still wired
      .build()) {

      StoreManagerWithSetterQualifier manager = context.get(StoreManagerWithSetterQualifier.class);
      assertThat(manager.blueStore()).isEqualTo("TD Blue Only");
      assertThat(manager.greenStore()).isEqualTo("green");
    }
  }

}
