package org.example.coffee.qualifier;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoreManagerWithSetterQualifierTest {

  @Test
  void redStore() {
    try (BeanContext context = BeanContext.newBuilder().build()) {
      StoreManagerWithSetterQualifier manager = context.getBean(StoreManagerWithSetterQualifier.class);
      assertThat(manager.blueStore()).isEqualTo("blue");
      assertThat(manager.greenStore()).isEqualTo("green");
    }
  }

  @Test
  void namedTestDouble() {
    try (BeanContext context = BeanContext.newBuilder()
      .withBean("Blue", SomeStore.class, () -> "TD Blue")
      .withBean("Green", SomeStore.class, () -> "TD Green")
      .build()) {

      StoreManagerWithSetterQualifier manager = context.getBean(StoreManagerWithSetterQualifier.class);
      assertThat(manager.blueStore()).isEqualTo("TD Blue");
      assertThat(manager.greenStore()).isEqualTo("TD Green");
    }
  }

  @Test
  void namedTestDouble_expect_otherNamedStillWired() {
    try (BeanContext context = BeanContext.newBuilder()
      .withBean("Blue", SomeStore.class, () -> "TD Blue Only")
      // with GreenStore still wired
      .build()) {

      StoreManagerWithSetterQualifier manager = context.getBean(StoreManagerWithSetterQualifier.class);
      assertThat(manager.blueStore()).isEqualTo("TD Blue Only");
      assertThat(manager.greenStore()).isEqualTo("green");
    }
  }

}
