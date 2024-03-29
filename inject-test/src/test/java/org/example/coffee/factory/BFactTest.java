package org.example.coffee.factory;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BFactTest {

  @Test
  public void getCountInit() {

    BFact bFact;
    try (BeanScope context = BeanScope.builder().build()) {
      bFact = context.get(BFact.class);
      assertThat(bFact.getCountInit()).isEqualTo(1);
      assertThat(bFact.getCountClose()).isEqualTo(0);
    }

    assertThat(bFact.getCountInit()).isEqualTo(1);
    assertThat(bFact.getCountClose()).isEqualTo(1);
  }
}
