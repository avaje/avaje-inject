package org.example.coffee.factory;

import io.dinject.Bean;
import io.dinject.Factory;
import org.example.coffee.factory.other.Something;

@Factory
public class MyFactory {

  @Bean
  CFact buildCFact() {
    return new CFact();
  }

  @Bean
  Something buildSomething() {
    return new SomeImpl();
  }
}
