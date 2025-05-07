package org.example.myapp.arrays;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class ArrayFactory {

  @Bean
  byte[] bytes() {
    return new byte[] {};
  }

  @Bean
  ArrayType[] type() {
    return new ArrayType[] {};
  }

  public static class ArrayType {}
}
