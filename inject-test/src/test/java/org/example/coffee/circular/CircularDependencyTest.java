package org.example.coffee.circular;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CircularDependencyTest {

  @Test
  void wire() {
    try (BeanScope context = BeanScope.newBuilder().build()) {
      assertThat(context.getBean(CircA.class)).isNotNull();
      assertThat(context.getBean(CircB.class)).isNotNull();
      assertThat(context.getBean(CircC.class)).isNotNull();

      final CircA circA = context.getBean(CircA.class);
      final CircB circB = context.getBean(CircB.class);
      final CircC circC = context.getBean(CircC.class);
      assertThat(circC.gen()).isEqualTo("C+CircA-CircB-CircC-Stop");

      assertThat(circC.circA).isSameAs(circA);
      assertThat(circA.circB).isSameAs(circB);
      assertThat(circB.circC).isSameAs(circC);
    }
  }
}
