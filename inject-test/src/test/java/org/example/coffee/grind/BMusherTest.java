package org.example.coffee.grind;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BMusherTest {

  @Test
  public void init() {

    BMusher bMusher;
    try (BeanScope context = BeanScope.builder().build()) {
      bMusher = context.get(BMusher.class);
      assertEquals(bMusher.getCountInit(), 1);
      assertEquals(bMusher.getCountClose(), 0);
    }

    assertEquals(bMusher.getCountInit(), 1);
    assertEquals(bMusher.getCountClose(), 1);
  }
}
