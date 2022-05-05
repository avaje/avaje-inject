package org.example.generic;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyFuncFactoryTest {

  @Test
  void test_does_wire() {
    try (BeanScope beanScope = BeanScope.builder().build()) {
      MyFunc myFunc = beanScope.get(MyFunc.class);

      assertThat(myFunc.apply("Hi")).isEqualTo("echo: Hi");
    }
  }
}
