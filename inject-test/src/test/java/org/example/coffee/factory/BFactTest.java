package org.example.coffee.factory;

import io.avaje.inject.BeanContext;
import io.avaje.inject.BeanContextBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BFactTest {

  @Test
  public void getCountInit() {

    BFact bFact;
    try (BeanContext context = new BeanContextBuilder().build()) {

      bFact = context.getBean(BFact.class);
      assertThat(bFact.getCountInit()).isEqualTo(1);
      assertThat(bFact.getCountClose()).isEqualTo(0);
    }

    assertThat(bFact.getCountInit()).isEqualTo(1);
    assertThat(bFact.getCountClose()).isEqualTo(1);
  }
}
