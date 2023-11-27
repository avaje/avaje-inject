package org.example.myapp.generic;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class GenericFactory {

  @Bean
  Generic<Integer> one() {

    return new Generic<>() {};
  }

  @Bean
  Stringy two() {

    return new Stringy() {};
  }

  public interface Stringy extends Generic<String> {}
}
