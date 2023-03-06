package org.example.myapp.config;

import io.avaje.inject.BeanScope;
import io.avaje.inject.test.TestBeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SomeOptionalUserTest {

  @Test
  void nullableIsAutoRequires() {
    try (BeanScope scope = TestBeanScope.builder().build()) {
      SomeOptionalUser someOptionalNullable = scope.get(SomeOptionalUser.class);
      assertThat(someOptionalNullable.hasOptionalDependency()).isFalse();

      SomeOptionalUser2 someOptional2 = scope.get(SomeOptionalUser2.class);
      assertThat(someOptional2.hasOptionalDependency()).isFalse();
    }
  }
}
