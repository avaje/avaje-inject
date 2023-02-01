package org.example.myapp;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.TestBeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SomeExternalAspectUserTest {

  @Test
  void hello() {
    try (BeanScope scope = TestBeanScope.builder().build()) {
      var some = scope.get(SomeExternalAspectUser.class);
      assertThat(some.hello()).isEqualTo("hello");
    }
  }
}
