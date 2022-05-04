package org.example.optional;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalSecondaryTest {

  @Test
  void uses_secondary_by_default() {
    try (BeanScope beanScope = BeanScope.builder().build()) {

      OFooBar one = beanScope.get(OFooBar.class);
      OFooBazz two = beanScope.get(OFooBazz.class);

      assertThat(one.fooey()).isEqualTo("forBar");
      assertThat(two.fooey()).isEqualTo("forBazz");
    }
  }

  @Test
  void uses_supplied_when_provided() {
    OFooService supplied = new Supplied();
    try (BeanScope beanScope = BeanScope.builder()
      .withBean("forBar", OFooService.class, supplied)
      .withBean("forBazz", OFooService.class, supplied)
      .build()) {

      OFooBar one = beanScope.get(OFooBar.class);
      OFooBazz two = beanScope.get(OFooBazz.class);

      assertThat(one.fooey()).isEqualTo("supplied");
      assertThat(two.fooey()).isEqualTo("supplied");
    }
  }

  static class Supplied implements OFooService {

    @Override
    public String fooey() {
      return "supplied";
    }
  }
}
