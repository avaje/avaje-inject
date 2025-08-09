package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(MyDestroyOrder2.ordering())
      .containsExactly(
        "Two",
        "One");
  }
}
