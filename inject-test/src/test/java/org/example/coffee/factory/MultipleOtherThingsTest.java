package org.example.coffee.factory;

import io.avaje.inject.BeanContext;
import io.avaje.inject.SystemContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipleOtherThingsTest {

  @Test
  void test() {
    try (BeanContext context = BeanContext.newBuilder().build()) {
      final MultipleOtherThings combined = context.getBean(MultipleOtherThings.class);
      assertEquals("blue", combined.blue());
      assertEquals("red", combined.red());
      assertEquals("green", combined.green());
      assertEquals("yellow", combined.yellow());
    }
  }

  @Test
  void named_case_insensitive() {
    Otherthing yellow0 = SystemContext.getBean(Otherthing.class, "yellow");
    Otherthing yellow1 = SystemContext.getBean(Otherthing.class, "Yellow");

    assertThat(yellow0.doOther()).isEqualTo("yellow");
    assertThat(yellow1).isSameAs(yellow0);
  }
}
