package org.example.coffee.factory;

import io.dinject.BeanContext;
import io.dinject.BootContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigurationTest {

  @Test
  public void getCountInit() {

    Configuration configuration;
    try (BeanContext context = new BootContext().load()) {
      configuration = context.getBean(Configuration.class);
      assertEquals(configuration.getCountInit(), 1);
      assertEquals(configuration.getCountClose(), 0);
    }

    assertEquals(configuration.getCountInit(), 1);
    assertEquals(configuration.getCountClose(), 1);
  }
}
