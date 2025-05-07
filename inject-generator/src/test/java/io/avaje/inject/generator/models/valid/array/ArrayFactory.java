package io.avaje.inject.generator.models.valid.array;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class ArrayFactory {

  @Bean
  byte[] bytes() {
    return null;
  }

  @Bean
  ArrayType[] type() {
    return null;
  }

  public static class ArrayType {}
}
