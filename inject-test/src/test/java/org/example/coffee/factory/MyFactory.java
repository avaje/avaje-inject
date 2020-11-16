package org.example.coffee.factory;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.example.coffee.factory.other.Something;

import jakarta.inject.Named;

@Factory
public class MyFactory {

  String methods = "";

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

  @Bean
  void useCFact(CFact cfact) {
    methods += "|useCFact";
  }

  @Bean
  void anotherCFact(CFact cfact) {
    methods += "|anotherCFact";
  }

  String methodsCalled() {
    return methods;
  }
}
