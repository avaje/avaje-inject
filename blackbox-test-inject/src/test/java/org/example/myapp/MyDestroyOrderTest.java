package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class MyDestroyOrderTest {

  @Test
  void ordering() {
    MyDestroyOrder.ordering().clear();
    try (BeanScope beanScope = BeanScope.builder().build()) {
      beanScope.get(HelloService.class);
    }
    List<String> ordering = MyDestroyOrder.ordering();
    assertThat(ordering)
        .containsExactly(
            "HelloService",
            "MyNamed",
            "AppConfig",
            "MyMetaDataRepo",
            "ExampleService",
            "AppHelloData");
  }
}
