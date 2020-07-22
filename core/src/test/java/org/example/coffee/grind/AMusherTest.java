package org.example.coffee.grind;

import io.dinject.BeanContext;
import io.dinject.BeanContextBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class AMusherTest {

  @Test
  public void getCountInit() {

    AMusher aMusher;
    try (BeanContext context = new BeanContextBuilder().build()) {
      aMusher = context.getBean(AMusher.class);
      assertEquals(aMusher.getCountInit(), 1);
      assertEquals(aMusher.getCountClose(), 0);
    }
    assertEquals(aMusher.getCountInit(), 1);
    assertEquals(aMusher.getCountClose(), 1);
  }
}
