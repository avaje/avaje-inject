package org.example.coffee.grind;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BMusherTest {

  @Test
  public void init() {

    BMusher bMusher;
    try (BeanContext context = BeanContext.newBuilder().build()) {
      bMusher = context.getBean(BMusher.class);
      assertEquals(bMusher.getCountInit(), 1);
      assertEquals(bMusher.getCountClose(), 0);
    }

    assertEquals(bMusher.getCountInit(), 1);
    assertEquals(bMusher.getCountClose(), 1);
  }
}
