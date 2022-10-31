package org.example.coffee.core;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithMultiCtorTest {

  @Test
  void injectConstructor_expect_notAmbiguous() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      WithMultiCtor withMultiCtor = beanScope.get(WithMultiCtor.class);

      assertThat(withMultiCtor.steamer).isNotNull();
    }
  }
}
