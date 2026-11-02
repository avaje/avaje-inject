package org.example.customannotation.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class CustomAnnotationTest {

  @Test
  void beansFromExternalCustomAnnotations() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      GreetService service = beanScope.get(GreetService.class);
      assertThat(service).isSameAs(beanScope.get(GreetService.class));
      assertThat(service.greet("bob")).isEqualTo("hello bob");

      GreetController one = beanScope.get(GreetController.class);
      GreetController two = beanScope.get(GreetController.class);
      assertThat(one).isNotSameAs(two);
      assertThat(one.service()).isSameAs(service);
    }
  }
}
