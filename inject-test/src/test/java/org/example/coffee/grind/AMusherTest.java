package org.example.coffee.grind;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AMusherTest {

  @Test
  public void getCountInit() {

    AMusher aMusher;
    try (BeanContext context = BeanContext.newBuilder().build()) {
      aMusher = context.getBean(AMusher.class);
      assertEquals(aMusher.getCountInit(), 1);
      assertEquals(aMusher.getCountClose(), 0);
    }
    assertEquals(aMusher.getCountInit(), 1);
    assertEquals(aMusher.getCountClose(), 1);
  }
}
