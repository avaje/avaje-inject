package org.example.coffee.factory;

import io.dinject.Bean;
import io.dinject.Factory;

@Factory
public class MyFactory {

  @Bean
  CFact buildCFact() {
    return new CFact();
  }
}
