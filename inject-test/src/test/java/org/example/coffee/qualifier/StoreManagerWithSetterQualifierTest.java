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
}
