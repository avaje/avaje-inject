package org.example.coffee.factory;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

  @Test
  public void getCountInit() {

    Configuration configuration;
    try (BeanScope context = BeanScope.builder().build()) {
      configuration = context.get(Configuration.class);
      assertEquals(configuration.getCountInit(), 1);
      assertEquals(configuration.getCountClose(), 0);
    }

    assertEquals(configuration.getCountInit(), 1);
    assertEquals(configuration.getCountClose(), 1);
  }
}
