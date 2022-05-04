package org.example.coffee.circular;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CircularDependencyTest {

  @Test
  void wire() {
    try (BeanScope context = BeanScope.builder().build()) {
      assertThat(context.get(CircA.class)).isNotNull();
      assertThat(context.get(CircB.class)).isNotNull();
      assertThat(context.get(CircC.class)).isNotNull();

      final CircA circA = context.get(CircA.class);
      final CircB circB = context.get(CircB.class);
      final CircC circC = context.get(CircC.class);
      assertThat(circC.gen()).isEqualTo("C+CircA-CircB-CircC-Stop");

      assertThat(circC.circA).isSameAs(circA);
      assertThat(circA.circB).isSameAs(circB);
      assertThat(circB.circC).isSameAs(circC);
    }
  }
}
