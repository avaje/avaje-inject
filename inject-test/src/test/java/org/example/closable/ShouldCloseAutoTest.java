package org.example.closable;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShouldCloseAutoTest {

  @Test
  void test() {

    ShouldCloseAuto shouldCloseAuto;
    try (final BeanScope scope = BeanScope.newBuilder().build()) {
      shouldCloseAuto = scope.get(ShouldCloseAuto.class);
    }

    assertThat(shouldCloseAuto.isClosed()).isTrue();
  }
}
