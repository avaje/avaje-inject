package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class MyDestroyOrderTest {

  @Test
  void ordering_byPriority() {
    MyDestroyOrder.ordering().clear();
    try (BeanScope beanScope = BeanScope.builder().build()) {
      beanScope.get(HelloService.class);
    }
    assertThat(MyDestroyOrder.ordering())
        .containsExactly(
            "HelloService",
            "MyNamed",
            "AppConfig",
            "MyMetaDataRepo",
            "ExampleService",
            "AppHelloData");
  }

  @Test
  void ordering_expect_reverseOfDependencies() {
    MyDestroyOrder2.ordering().clear();
    try (BeanScope beanScope = BeanScope.builder().build()) {
      beanScope.get(HelloService.class);
    }
    assertThat( MyDestroyOrder2.ordering())
      .containsExactly(
        "Two",
        "One");
  }
}
