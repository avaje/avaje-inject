package org.example.myapp.i347;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyMoServiceTest {
  @Test
  void wiredOk() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      final var myMoService = beanScope.get(MyMoService.class);
      assertThat(myMoService).isNotNull();
    }
  }
}
