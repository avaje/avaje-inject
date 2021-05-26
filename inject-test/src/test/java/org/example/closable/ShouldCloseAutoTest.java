package org.example.closable;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShouldCloseAutoTest {

  @Test
  void test_closable() {

    ShouldCloseAuto shouldCloseAuto;
    try (final BeanScope scope = BeanScope.newBuilder().build()) {
      shouldCloseAuto = scope.get(ShouldCloseAuto.class);
    }

    assertThat(shouldCloseAuto.isClosed()).isTrue();
  }

  @Test
  void test_autoCloseable() {

    ShouldCloseAuto2 shouldCloseAuto;
    try (final BeanScope scope = BeanScope.newBuilder().build()) {
      shouldCloseAuto = scope.get(ShouldCloseAuto2.class);
    }

    assertThat(shouldCloseAuto.isClosed()).isTrue();
  }
}
