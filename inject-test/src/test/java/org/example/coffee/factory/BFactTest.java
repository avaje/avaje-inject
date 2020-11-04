package org.example.coffee.factory;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BFactTest {

  @Test
  public void getCountInit() {

    BFact bFact;
    try (BeanContext context = BeanContext.newBuilder().build()) {
      bFact = context.getBean(BFact.class);
      assertThat(bFact.getCountInit()).isEqualTo(1);
      assertThat(bFact.getCountClose()).isEqualTo(0);
    }

    assertThat(bFact.getCountInit()).isEqualTo(1);
    assertThat(bFact.getCountClose()).isEqualTo(1);
  }
}
