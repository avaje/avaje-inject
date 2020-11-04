package org.example.coffee.factory;

import io.avaje.inject.BeanContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

  @Test
  public void getCountInit() {

    Configuration configuration;
    try (BeanContext context = BeanContext.newBuilder().build()) {
      configuration = context.getBean(Configuration.class);
      assertEquals(configuration.getCountInit(), 1);
      assertEquals(configuration.getCountClose(), 0);
    }

    assertEquals(configuration.getCountInit(), 1);
    assertEquals(configuration.getCountClose(), 1);
  }
}
