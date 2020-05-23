package org.example.coffee.factory;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OtherThingTest {

  @Test
  public void test() {

    try (BeanContext context = new BootContext().load()) {
      final TwoOtherThings combined = context.getBean(TwoOtherThings.class);
      assertEquals("blue", combined.blue());
      assertEquals("red", combined.red());
    }
  }
}
