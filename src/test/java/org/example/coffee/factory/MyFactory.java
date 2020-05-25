package org.example.coffee.factory;

import io.dinject.Bean;
import io.dinject.Factory;
import org.example.coffee.factory.other.Something;

import javax.inject.Named;

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

  @Bean
  @Named("green")
  Otherthing greenOther() {
    return () -> "green";
  }

  @Bean
  @Named("yellow")
  Otherthing yellowOther() {
    return () -> "yellow";
  }
}
