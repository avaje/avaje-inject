package org.example.myapp.other;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class NestedTest {

  @Test
  void leaf2IsWired() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.get(Nested.Leaf2.class)).isNotNull();
    }
  }
}
