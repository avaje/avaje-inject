package org.example.myapp.customannotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class CustomAnnotationTest {

  @Test
  void customSingleton() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      CustomService one = beanScope.get(CustomService.class);
      CustomService two = beanScope.get(CustomService.class);
      assertThat(one).isNotNull();
      assertThat(one).isSameAs(two);
    }
  }

  @Test
  void customPrototype() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      CustomController one = beanScope.get(CustomController.class);
      CustomController two = beanScope.get(CustomController.class);
      assertThat(one).isNotNull();
      assertThat(one).isNotSameAs(two);
      assertThat(one.service()).isSameAs(two.service());
    }
  }

  @Test
  void customPrototypeFactoryMethod() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      var one = beanScope.get(CustomFactory.Widget.class);
      var two = beanScope.get(CustomFactory.Widget.class);
      assertThat(one).isNotNull();
      assertThat(one).isNotSameAs(two);
    }
  }
}
