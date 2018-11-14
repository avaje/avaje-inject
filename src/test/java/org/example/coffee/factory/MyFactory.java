package org.example.coffee.factory;

import io.kanuka.Bean;
import io.kanuka.Factory;

@Factory
public class MyFactory {

  @Bean
  CFact buildCFact() {
    return new CFact();
  }
}
