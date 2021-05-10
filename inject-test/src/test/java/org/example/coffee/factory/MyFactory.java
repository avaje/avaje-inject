package org.example.coffee.factory;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.example.coffee.factory.other.Something;

import jakarta.inject.Named;
import org.example.coffee.parent.DesEngi;

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

  @Bean
  @Named("BuildDesi1")
  DesEngi buildEngi() {
    methods += "|buildEngi1";
    return new DesEngi(){
      @Override
      public String ignite() {
        return "buildEngi1";
      }
    };
  }

  @Bean
  @Named("BuildDesi2")
  DesEngi buildEngi2() {
    methods += "|buildEngi2";
    return new MyEngi();
  }

  String methodsCalled() {
    return methods;
  }

  private class MyEngi extends DesEngi {
    @Override
    public String ignite() {
      return "MyEngi";
    }
  }
}
