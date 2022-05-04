package org.example.optional;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalWithDefaultTest {

  @Test
  void whenOptionalEmpty_fallback() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      OptBax bax = beanScope.get(OptBax.class);
      assertThat(bax.hi()).isEqualTo("defaultOptionalBax");
    }
  }

  @Test
  void whenOptionalSupplied() {
    OptionalService supplied = new Supplied();
    try (BeanScope beanScope = BeanScope.builder()
      .bean("supplied", OptionalService.class, supplied)
      .build()) {
      OptBax bax = beanScope.get(OptBax.class);
      assertThat(bax.hi()).isEqualTo("supplied");
    }
  }

  static class Supplied implements OptionalService {

    @Override
    public String hi() {
      return "supplied";
    }
  }
}
