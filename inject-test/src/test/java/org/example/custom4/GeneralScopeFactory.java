package org.example.custom4;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class GeneralScopeFactory {

  @Bean
  A genaralScope() {
    return new A();
  }

  //@BuildScope
  @Bean
  B buildScope() {
    return new B();
  }


  public static class A {

  }

  public static class B {

  }
}
