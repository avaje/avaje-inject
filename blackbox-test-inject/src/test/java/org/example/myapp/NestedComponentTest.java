package org.example.myapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.inject.BeanScope;

class NestedComponentTest {

  @Test
  void nestedComponentIsWired() {
    try (BeanScope scope = BeanScope.builder().build()) {
      assertThat(scope.get(NestedComponent.Inner.class)).isNotNull();
    }
  }
}
