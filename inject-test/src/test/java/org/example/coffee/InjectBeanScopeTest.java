package org.example.coffee;

import io.avaje.inject.BeanScope;
import org.example.optional.Que;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that we can inject BeanScope.
 */
class InjectBeanScopeTest {

  @Test
  void test() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      INeedBeanScope need = beanScope.get(INeedBeanScope.class);
      Que queue = need.getQueue();

      String result = queue.push("hi");
      assertThat(result).isEqualTo("hi|frodo");
    }
  }
}
